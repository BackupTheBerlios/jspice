package tads2.jetty;

import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.util.Vector;

public class GameFileParser extends JettyRef {

	private int pos = 0;       // position we're at in the file
	private int xor_seed = 63;
	private int xor_inc = 64;
	private boolean encrypt_mode = true; // encrypt strings?

	public GameFileParser( Jetty jetty ) {
		super( jetty );
	}

	public GameState read( InputStream f ) throws IOException, HaltTurnException {
		// make it buffered:
		InputStream file = new BufferedInputStream( f );

		GameState state = new GameState( this.getJetty() );

		String tads_id = read_to_char( file, '\0' );
		if ( !"TADS2 bin\012\015\032".equals( tads_id ) ) {
			print_error( "This does not appear to be a TADS game file", 0 );
			return null;
		}

		String version = read_to_char( file, '\0' );
		if ( !"v2.2.0".equals( version ) ) {
			print_error( "Sorry, Jetty can only play games compiled with the most recent version (v2.2.0) of TADS", 0 );
			return null;
		}

		int game_flags = read_16( file );
		// 0x08: FIOFCRYPT - "encrypt" objects prior to writing them:
		encrypt_mode = ( ( game_flags & 0x08 ) != 0 );

		String timestamp = read_to_char( file, '\0' );

		state.set_version( version );
		state.set_flags( game_flags );
		state.set_timestamp( timestamp );

		while ( true ) {
			int name_size = read_8( file );
			String blockname = new String( read_amount( file, name_size ) );
			int next = read_32signed( file );

			int blocksize = next - pos + 1; // a much more useful number

			print_error( "Reading block: " + blockname, 2 );

			if ( blockname.equals( "XSI" ) ) {
				proc_XSI_block( file, blocksize, state );
			} else if ( blockname.equals( "VOC" ) ) {
				proc_VOC_block( file, blocksize, state );
			} else if ( blockname.equals( "CMPD" ) ) {
				proc_CMPD_block( file, blocksize, state );
			} else if ( blockname.equals( "SPECWORD" ) ) {
				proc_SPECWORD_block( file, blocksize, state );
			} else if ( blockname.equals( "INH" ) ) {
				proc_INH_block( file, blocksize, state );
			} else if ( blockname.equals( "REQ" ) ) {
				proc_REQ_block( file, blocksize, state );
			} else if ( blockname.equals( "PREINIT" ) ) {
				proc_PREINIT_block( file, blocksize, state );
			} else if ( blockname.equals( "FMTSTR" ) ) {
				proc_FMTSTR_block( file, blocksize, state );
			} else if ( blockname.equals( "OBJ" ) ) {
				proc_OBJ_block( file, blocksize, state );
			} else if ( blockname.equals( "$EOF" ) ) {
				print_error( "Reached EOF. Exiting.", 2 );
				break;
			} else if ( blockname.equals( "XFCN" ) || blockname.equals( "FST" ) ||
				blockname.equals( "HTMLRES" ) ) {
				print_error( "Ignoring " + blockname + " block", 2 );
				read_amount( file, blocksize );
			} else if ( blockname.equals( "PREINIT" ) ||
				blockname.equals( "SYMTAB" ) ||
				blockname.equals( "SRC" ) ||
				blockname.equals( "SRC2" ) ) {
				print_error( "Skipping debug-only block type: " + blockname, 2 );
				read_amount( file, blocksize );
			} else {
				print_error( "Unknown block type: " + blockname, 2 );
				read_amount( file, blocksize );
			}
		}

		return state;
	}

	private void proc_XSI_block( InputStream file, int size, GameState state )
		throws IOException {
		xor_seed = read_8( file );
		xor_inc = read_8( file );
	}

	private void proc_PREINIT_block( InputStream file, int size, GameState state )
		throws IOException {
		state.set_preinit( read_16signed( file ) );
	}

	private void proc_VOC_block( InputStream file, int size, GameState state )
		throws IOException, HaltTurnException {
		while ( size > 0 ) {
			// there's two lengths, the latter for the preposition in, eg, 'knockon'
			int len = read_16( file );
			int len2 = read_16( file );
			int type = read_16( file );
			int obj = read_16( file );
			int flags = read_16( file );

			String word = new String( encrypt( read_amount( file, len + len2 ) ) );
			String word2 = null;
			if ( len2 > 0 ) {
				word2 = word.substring( len );
				word = word.substring( 0, len );
			}

			size -= ( 10 + len + len2 );

			state.add_vocab( obj, type, flags, word, word2 );
		}
	}

	private void proc_CMPD_block( InputStream file, int size, GameState state )
		throws IOException {
		int length = read_16( file );
		byte[] data = encrypt( read_amount( file, length ) );

		for ( int i = 0; i < data.length; ) {
			int len = read_16( data, i ) - 2;
			String word1 = new String( data, i + 2, len );
			i += len + 2;

			len = read_16( data, i ) - 2;
			String word2 = new String( data, i + 2, len );
			i += len + 2;

			len = read_16( data, i ) - 2;
			String word3 = new String( data, i + 2, len );
			i += len + 2;

			state.add_compound_word( word1, word2, word3 );
		}
	}

	private void proc_SPECWORD_block( InputStream file, int size, GameState state )
		throws IOException {
		int length = read_16( file );
		byte[] data = encrypt( read_amount( file, length ) );

		for ( int i = 0; i < data.length; ) {
			char abbr = (char)read_8( data, i );
			int len = read_8( data, i + 1 );
			String word = new String( data, i + 2, len );
			i += len + 2;

			state.add_specword( abbr, word );
		}
	}

	private void proc_INH_block( InputStream file, int size, GameState state )
		throws IOException {
		while ( size > 0 ) {
			int flags = read_8( file );
			int obj = read_16( file );
			int loc = read_16signed( file );
			int inhloc = read_16signed( file );
			int sc_count = read_16( file );
			int[] scs = new int[ sc_count ];
			for ( int i = 0; i < sc_count; i++ ) {
				scs[ i ] = read_16( file );
			}

			size -= 9 + sc_count * 2;

			state.add_inheritance( obj, loc, inhloc, flags, scs );
		}
	}

	private void proc_REQ_block( InputStream file, int size, GameState state )
		throws IOException {
		int[] vals = new int[ RequiredObjects.COUNT ];
		// surely there's a way to initialize it in the declaration
		for ( int i = 0; i < vals.length; i++ ) {
			vals[ i ] = -1;
		}

		byte[] data = read_amount( file, size );

		for ( int i = 0; i < vals.length; i++ ) {
			if ( i >= size / 2 ) {
				break; // not all of the required things are given in
			}
			// older gamefiles
			vals[ i ] = read_16signed( data, i * 2 );
		}

		state.set_required( vals );
	}

	private void proc_FMTSTR_block( InputStream file, int size, GameState state )
		throws IOException {
		int length = read_16( file );
		byte[] data = encrypt( read_amount( file, length ) );

		for ( int i = 0; i < data.length; ) {
			int prop = read_16( data, i );
			int len = read_16( data, i + 2 ) - 2;
			String word = new String( data, i + 4, len );
			i += len + 4;

			state.add_fmtstr( word, prop );
		}
	}

	private void proc_OBJ_block( InputStream file, int size, GameState state )
		throws IOException {
		while ( size > 0 ) {
			int type = read_8( file );
			int id = read_16signed( file );
			size -= 3;

			if ( type == 1 ) // function decl
			{
				int len = read_16( file );
				int cache_len = read_16( file );
				size -= ( 4 + cache_len );
				byte[] data = encrypt( read_amount( file, cache_len ) );

				state.add_function_object( id, data );
			} else if ( type == 2 ) // object decl
			{
				int len = read_16( file );
				int cache_len = read_16( file );
				size -= ( 4 + cache_len );
				byte[] data = encrypt( read_amount( file, cache_len ) );

				int idx = 0;
				int workspace = read_16( data, idx );
				int flags = read_16( data, idx + 2 );
				int num_sc = read_16( data, idx + 4 );
				int num_props = read_16( data, idx + 6 );
				int free_offset = read_16( data, idx + 8 );
				int num_static_props = read_16( data, idx + 12 );
				int static_size = read_16( data, idx + 10 );
				idx += 14;

				int[] scs = new int[ num_sc ];
				for ( int i = 0; i < num_sc; i++, idx += 2 ) {
					scs[ i ] = read_16( data, idx );
				}

				if ( ( flags & 2 ) != 0 ) // 2 = there's an index, which we will just skip
				{
					idx = read_16( data, idx ); // read the offset of the properties and
				}
				// jump there

				TProperty[] props = new TProperty[ num_props ];

				// now read out all the properties
				int pcount = 0;
				while ( idx < data.length ) {
					int prop = read_16( data, idx );
					int ptype = read_8( data, idx + 2 );
					int psize = read_16( data, idx + 3 );
					int pflags = read_8( data, idx + 5 );
					idx += 6;
					// for some reason, lists, strings, and dstrings store their size at
					// the start, even though we already know it from psize
					if ( ptype == TValue.LIST || ptype == TValue.SSTRING || ptype == TValue.DSTRING ) {
						idx += 2;
						psize -= 2;
					}
					byte[] value = new byte[ psize ];
					System.arraycopy( data, idx, value, 0, value.length );
					idx += psize;
					TValue tv = newTValue( ptype, value );
					props[ pcount++ ] = new TProperty( prop, tv );
				}

				state.add_object_object( id, flags, scs, props );
			} else if ( type == 7 || type == 8 ) // func/obj predecl, ignore it
			{
				int len = read_16( file );
				read_amount( file, len );
				size -= 2 + len;
			} else if ( type == 10 ) // XFCN decl, ignore it
			{
				int len = read_8( file );
				read_amount( file, len );
				size -= 1 + len;
			} else {
				print_error( "Unknown object block: " + type, 1 );
			}
		}
	}

	private byte[] encrypt( byte[] data ) {
		// Gordon K pointed out Jetty wasn't correctly handling games which
		// don't have encryption turned on
		if ( !encrypt_mode ) {
			return data;  // this is ok, since data is modified in-place below:
		}

		int xor_value = xor_seed;

		for ( int i = 0; i < data.length; i++, xor_value += xor_inc ) {
			data[ i ] ^= xor_value;
		}

		// this is just for convenience, since of course data has been
		// modified in-place:
		return data;
	}

	private String read_to_char( InputStream file, char stop )
		throws IOException {
		StringBuffer sb = new StringBuffer();
		char c;
		while ( ( c = (char)file.read() ) != stop ) {
			sb.append( c );
		}
		pos += sb.length() + 1;
		return sb.toString();
	}

	private byte[] read_amount( InputStream file, int length )
		throws IOException {
		byte[] b = new byte[ length ];
		int i = 0;
		while ( i < b.length ) {
			int c = file.read( b, i, ( b.length - i ) );
			if ( c == -1 ) {
				throw new IOException( "Premature end of file reached" );
			} else {
				i += c;
			}
		}

		pos += length;
		return b;
	}

	// this one doesn't do anything, it's just provided for so it looks
	// the same as reading shorts and longs
	private int read_8( InputStream file )
		throws IOException {
		pos += 1;
		return file.read();
	}

	private int read_16( InputStream file )
		throws IOException {
		pos += 2;

		int c1 = file.read();
		int c2 = file.read();

		return ( c2 << 8 ) + c1;
	}

	private int read_16signed( InputStream file )
		throws IOException {
		pos += 2;

		int c1 = file.read();
		int c2 = file.read();
		int sum = ( c2 << 8 ) + c1;

		return ( sum < 1 << 15 ) ? sum : sum - ( 1 << 16 );
	}

	private int read_32( InputStream file )
		throws IOException {
		pos += 4;

		int c1 = file.read();
		int c2 = file.read();
		int c3 = file.read();
		int c4 = file.read();

		return ( c4 << 24 ) + ( c3 << 16 ) + ( c2 << 8 ) + c1;
	}

	private int read_32signed( InputStream file )
		throws IOException {
		pos += 4;

		int c1 = file.read();
		int c2 = file.read();
		int c3 = file.read();
		int c4 = file.read();

		int sum = ( c4 << 24 ) + ( c3 << 16 ) + ( c2 << 8 ) + c1;

		// this seems like it should be right but it causes problems. I don't know
		// if this is because there is another bug elsewhere which cancels this,
		// or because this is genuinely different than the other read32_signed.
		//    return (sum < ((long)1<<31)) ? sum : (int)(sum - ((long)1<<32));
		return ( sum < 1 << 31 ) ? sum : sum - ( 1 << 32 );
	}

	// this one doesn't do anything, it's just provided for so it looks
	// the same as reading shorts and longs
	public static int read_8( byte[] data, int offset ) {
		return data[ offset ];
	}

	public static int read_16( byte[] data, int offset ) {
		int c1 = data[ offset ] & 0xff;
		int c2 = data[ offset + 1 ] & 0xff;

		return ( c2 << 8 ) + c1;
	}

	public static int read_16signed( byte[] data, int offset ) {
		int c1 = data[ offset ] & 0xff;
		int c2 = data[ offset + 1 ] & 0xff;
		int sum = ( ( c2 << 8 ) + c1 );

		return ( sum < 1 << 15 ) ? sum : sum - ( 1 << 16 );
	}

	public static int read_32( byte[] data, int offset ) {
		int c1 = data[ offset ] & 0xff;
		int c2 = data[ offset + 1 ] & 0xff;
		int c3 = data[ offset + 2 ] & 0xff;
		int c4 = data[ offset + 3 ] & 0xff;

		return ( c4 << 24 ) + ( c3 << 16 ) + ( c2 << 8 ) + c1;
	}

	public static int read_32signed( byte[] data, int offset ) {
		int c1 = data[ offset ] & 0xff;
		int c2 = data[ offset + 1 ] & 0xff;
		int c3 = data[ offset + 2 ] & 0xff;
		int c4 = data[ offset + 3 ] & 0xff;

		int sum = ( c4 << 24 ) + ( c3 << 16 ) + ( c2 << 8 ) + c1;

		return ( sum < ( (long)1 << 31 ) ) ? sum : (int)( sum - ( (long)1 << 32 ) );
	}
}
