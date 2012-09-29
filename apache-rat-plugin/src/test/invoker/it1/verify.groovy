content = new File( basedir, 'target/rat.txt' ).text;

assert content.contains( 'YAL__ src.apt' );