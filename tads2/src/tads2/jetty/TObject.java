package tads2.jetty;

import java.util.Vector;

public class TObject extends JettyRef {

	public TObject( final Jetty jetty, int id, int f, int[] scs, TProperty[] p ) {
		super( jetty );
		_id = id;
		_flags = f;
		_properties = p;
		_superclasses = scs;
		_vocab = new Vector();
		_data = null;
	}

	public TObject( final Jetty jetty, int id, byte[] data ) {
		super( jetty );
		_id = id;
		_data = data;
	}

	public int get_id() {
		return _id;
	}

	public byte[] get_data() {
		return _data;
	}

	public int[] get_superclasses() {
		return _superclasses;
	}

	public Vector get_vocab() {
		return _vocab;
	}

	public boolean is_object() {
		return _data == null;
	}

	public boolean is_class() {
		return is_object() && ( ( _flags & 0x01 ) != 0 );
	}

	public boolean is_dynamic() {
		return is_object() && ( ( _flags & 0x016 ) != 0 );
	}

	public boolean is_numbered() {
		return _is_numbered;
	}

	public void add_vocab( VocabWord vw ) {
		if ( !_vocab.contains( vw ) ) {
			_vocab.addElement( vw );
		}
		if ( vw.get_word().equals( "#" ) ) {
			_is_numbered = true;
		}
	}

	public void rem_vocab( VocabWord vw ) {
		for ( int i = 0; i < _vocab.size(); i++ ) {
			VocabWord v = (VocabWord)_vocab.elementAt( i );
			if ( v.get_word().equals( vw.get_word() ) ) {
				_vocab.removeElementAt( i );
				i--;
			}
		}

		if ( vw.get_word().equals( "#" ) ) {
			_is_numbered = false;
		}
	}

	public boolean inherits_from( int sc ) throws HaltTurnException {
		for ( int i = 0; i < _superclasses.length; i++ ) {
			if ( _superclasses[ i ] == sc ) {
				return true;
			} else {
				TObject obj = getState().lookup_object( _superclasses[ i ] );
				if ( obj.inherits_from( sc ) ) {
					return true;
				}
			}
		}

		return false;
	}

	// this returns the TValue object itself
	public TValue lookup_property( int pid )
		throws HaltTurnException {
		return lookup_property( pid, true, true );
	}

	public TValue lookup_property( int pid, boolean recurse )
		throws HaltTurnException {
		return lookup_property( pid, recurse, true );
	}

	public TValue lookup_property( int pid, boolean recurse, boolean use_first )
		throws HaltTurnException {
		return lookup_property( pid, recurse, use_first, null );
	}

	public TValue lookup_property( int pid, boolean recurse, boolean use_first,
		TObject[] source )
		throws HaltTurnException {
		if ( get_debug_level() >= 3 ) {
			print_error( "Looking up self=" + get_id() + " property=" + pid, 3 );
		}

		TValue p = lookup_local_property( pid );
		if ( p != null ) {
			if ( use_first ) {
				if ( source != null ) {
					source[ 0 ] = this;
				}
				return p;
			} else {
				if ( get_debug_level() >= 3 ) {
					print_error( "Found locally, but use_first was false", 3 );
				}
				use_first = true;
			}
		}

		if ( !recurse ) {
			return null;
		}

		if ( get_debug_level() >= 3 ) {
			print_error( "Not found locally, inheriting", 3 );
		}

		// Note: TADS does *not* use the standard Java/C++/etc inheritance
		// algorithm. Instead it uses a kind of wacky thing that as far as I
		// know is unique to TADS and is designed purely to deal with
		// diamond inheritance. Specifically, here is the method:
		// the property to use is the one that is defined by the leftmost
		// superclass of the object where none of the superclasses of the
		// object to the right of that one are children of the superclass
		// which actually defines the property. I know, this isn't any
		// clearer than what's in the manual, so I will draw a picture:
		//     A
		//     | \      (B and C are the two children of A; E is the child
		//     | B       of C and D)
		//     | |
		//     C D
		//     \/
		//     E
		//
		// Ok, so say a property is defined by A and B. Under a C++ inheritance
		// model, you say "ok, it's equally close to E in A and B, and A is
		// leftmost (as it comes from C), so we use the one in A". In TADS,
		// however, we say "in superclass C, it comes from A. in superclass D,
		// it comes from B. therefore we use the one from A, since it's
		// the leftmost -- *unless* B (the other place where we found it)
		// inherits from A. Lo and behold it does, so we use B instead." And then
		// we would have to consider other superclasses to the right to see
		// if they inherit from B and so on. Got it? Good.

		TObject best_source = null;
		TValue best_value = null;

		for ( int i = 0; i < _superclasses.length; i++ ) {
			TObject sup = getState().lookup_object( _superclasses[ i ] );
			TObject[] s = new TObject[ 1 ];
			TValue v = sup.lookup_property( pid, true, true, s );

			if ( v == null ) {
				continue;
			}

			if ( best_value == null || s[ 0 ].inherits_from( best_source.get_id() ) ) {
				if ( !use_first ) {
					if ( get_debug_level() >= 3 ) {
						print_error( "Found in superclass, but use_first was false", 3 );
					}
					use_first = true;
					continue;
				}

				if ( get_debug_level() >= 3 ) {
					print_error( "Found in superclass=" + s[ 0 ].get_id() +
						"; using new value", 3 );
				}
				best_value = v;
				best_source = s[ 0 ];
			} else if ( get_debug_level() >= 3 ) {
				print_error( "Found in superclass=" + s[ 0 ].get_id() +	" but not using that", 3 );
			}
		}

		if ( source != null ) {
			source[ 0 ] = best_source;
		}
		return best_value;
	}

	public TValue lookup_local_property( int pid ) {
		if ( get_debug_level() >= 4 ) {
			print_error( "Doing lookup for id=" + _id + ", pid=" + pid, 3 );
		}

		if ( _properties == null ) {
			print_error( "lookup_property() called on function (self=" + _id + " pid=" + pid + ")", 1 );
			return null;
		}

		for ( int i = 0; i < _properties.length; i++ ) {
			if ( _properties[ i ].id == pid ) {
				return _properties[ i ].value;
			}
		}

		return null;
	}



	public static TValue[] arg_array( final JettyRef j, TValue t1, TValue t2, TValue t3, TValue t4,
		TValue t5 ) {
		TValue[] args = new TValue[ 5 ];
		args[ 0 ] = ( t1 == null ) ? j.newTValue( TValue.NIL, 0 ) : t1;
		args[ 1 ] = ( t2 == null ) ? j.newTValue( TValue.NIL, 0 ) : t2;
		args[ 2 ] = ( t3 == null ) ? j.newTValue( TValue.NIL, 0 ) : t3;
		args[ 3 ] = ( t4 == null ) ? j.newTValue( TValue.NIL, 0 ) : t4;
		args[ 4 ] = ( t5 == null ) ? j.newTValue( TValue.NIL, 0 ) : t5;
		return args;
	}

	// this returns a *copy* of the TValue object, and nil if it's
	// not defined
	public TValue eval_property( int pid, TValue[] args )
		throws ParseException, ReparseException, HaltTurnException,
		GameOverException {
		return eval_property( this, pid, args );
	}

	public TValue eval_property( TObject self, int pid, TValue[] args )
		throws ParseException, ReparseException, HaltTurnException,
		GameOverException {
		return eval_property( self, pid, args, true );
	}

	public TValue eval_property( TObject self, int pid, TValue[] args,
		boolean use_first )
		throws ParseException, ReparseException, HaltTurnException,
		GameOverException {
		if ( _properties == null ) {
			print_error( "eval_property() called on function (self=" + _id + " pid=" + pid + ")", 1 );
			Thread.dumpStack();
			return newTValue( TValue.NIL, 0 );
		}

		TObject[] source_obj = new TObject[ 1 ];
		TValue prop = lookup_property( pid, true, use_first, source_obj );
		if ( prop == null ) {
			return newTValue( TValue.NIL, 0 );
		} else if ( prop.get_type() == TValue.CODE ) {
			byte[] code = prop.get_code();
			if ( get_debug_level() >= 3 ) {
				print_error( "Evaluating id=" + _id + " pid=" + pid, 3 );
			}
			return getRunner().run( code, args, self, source_obj[ 0 ] );
		} else if ( prop.get_type() == TValue.DSTRING ) {
			print( prop.get_dstring() );
			return newTValue( TValue.NIL, 0 );
		} else if ( prop.get_type() == TValue.REDIR ) {
			TObject redir = getState().lookup_object( prop.get_redir() );
			return redir.eval_property( pid, args );
		} else if ( prop.get_type() == TValue.SYNONYM ) {
			return eval_property( prop.get_synonym(), args );
		} else {
			return prop;
		}
	}

	public void set_property( int pid, TValue value ) {
		set_property( pid, value, true );
	}

	public void set_property( int pid, TValue value, boolean save_undo ) {
		if ( _properties == null ) {
			print_error( "set_property() called on function (self=" + _id + " pid=" + pid + ")", 1 );
			return;
		}

		for ( int i = 0; i < _properties.length; i++ ) {
			if ( _properties[ i ].id == pid ) {
				if ( save_undo ) {
					getState().save_prop_undo( this, pid, _properties[ i ].value );
				}

				_properties[ i ].value = value;
				return;
			}
		}

		if ( save_undo ) {
			getState().save_prop_undo( this, pid, null );
		}

		TProperty[] tmp = new TProperty[ _properties.length + 1 ];
		System.arraycopy( _properties, 0, tmp, 0, _properties.length );
		tmp[ _properties.length ] = new TProperty( pid, value );
		_properties = tmp;
	}

	// note that this only deletes it from the local object, not from any
	// parent
	public void del_property( int pid ) {
		if ( _properties == null ) {
			print_error( "del_property() called on function (self=" + _id + " pid=" + pid + ")", 1 );
			return;
		}

		for ( int i = 0; i < _properties.length; i++ ) {
			if ( _properties[ i ].id == pid ) {
				TProperty[] tmp = new TProperty[ _properties.length - 1 ];
				System.arraycopy( _properties, 0, tmp, 0, i );
				System.arraycopy( _properties, i + 1, tmp, i, tmp.length - i );
				_properties = tmp;
				break;
			}
		}
	}

	private int _id;

	// if it's an object, it has these:
	private int _flags;
	private TProperty[] _properties;
	private int[] _superclasses;
	private Vector _vocab;
	private boolean _is_numbered = false;

	// and if it's a function, it has this one:
	private byte[] _data;
}
