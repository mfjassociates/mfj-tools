package net.mfjassociates.tools;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is thread safe.
 * @author MXJ037
 *
 */
public class CharsetStreamSupport {
//	public static final Charset cs=Charset.forName("UTF-8");
	public static final Logger logger=LoggerFactory.getLogger(CharsetStreamSupport.class);
	private final String charsetName;
	private ThreadLocal<CharsetDecoder> csDecoder=new ThreadLocal<CharsetDecoder>() {
		@Override protected CharsetDecoder initialValue() {
			return charset.newDecoder();
		}
	};
	private final Charset charset;
	public Charset getCharset() {
		return charset;
	}
	private final ThreadLocal<Boolean> initialized=new ThreadLocal<Boolean>() {
		@Override protected Boolean initialValue() {
			return false;
		}
	};
	
	public CharsetStreamSupport(String aCharsetName) {
		this.charsetName=aCharsetName;
		charset=Charset.forName(charsetName);
	}
	public CoderResult decode(CharBuffer cb, ByteBuffer bb, boolean eoi) {
		if (!initialized.get()) {
			csDecoder.get().reset();
			initialized.set(true);
		}
		CoderResult res = csDecoder.get().decode(bb, cb, eoi);
		if (eoi) csDecoder.get().flush(cb);
		return res;
	}
	/**
	 * This is from this stackoverflow:
	 * https://stackoverflow.com/a/29560129
	 * This shows how to call decode with false as end of input argument
	 * @param args
	 */
	public static void main2(String[] args) {
		final String pound="€";
		final Charset charset=Charset.forName("UTF-8");
		final byte[] tab = pound.getBytes(charset); //char €
		final CharsetDecoder dec = charset.newDecoder();
		final int charsLen=(int)(dec.maxCharsPerByte() * (double)tab.length);
		final CharBuffer chars = CharBuffer.allocate(charsLen);
		logger.info("byte[]={}, chars length={} maxCharsPerByte={}", toString(toHex(tab)), charsLen, dec.maxCharsPerByte());

		final ByteBuffer buffer = ByteBuffer.allocate(10);

		for (int i = 0; i < tab.length; i++) {
		    // Add the next byte to the buffer
		    buffer.put(tab[i]);

		    // Remember the current position
		    final int pos = buffer.position();

		    // Try to decode
		    buffer.flip();
		    final CoderResult result = dec.decode(buffer, chars, i == (tab.length-1));
		    logger.info("buffer remaining={} pos(before/now)={}/{} limit={} capacity={} result={}",
		    		buffer.remaining(), pos, buffer.position(),
		    		buffer.limit(), buffer.capacity(), result);

		    if (result.isUnderflow()) {
		        // Underflow, prepare the buffer for more writing
		        buffer.limit(buffer.capacity());
		        buffer.position(pos);
		    }
		}

		dec.flush(chars);
		chars.flip();
		logger.info("decoded=\"{}\"", chars.toString());
		
	}
	
	public float averageCharsPerByte() {
		return csDecoder.get().averageCharsPerByte();
	}
	public static void main(String[] args) {
		int position;
		int limit;
		int charSize;
		int byteSize;
		CharsetStreamSupport css = new CharsetStreamSupport("UTF-16");
		byte[] utf8bytes=new String("élèves").getBytes(css.getCharset());
		byteSize=utf8bytes.length;
		charSize=(int)(((double)byteSize)*css.averageCharsPerByte());
		limit=byteSize;
		limit=5;
		position=0;
		logger.info(toString(toHex(utf8bytes)));
		CharBuffer cb=CharBuffer.allocate(charSize);
		ByteBuffer bb=ByteBuffer.wrap(utf8bytes, position, limit);
		CoderResult res = css.decode(cb, bb, limit==byteSize);
		if (cb.hasArray()) {
			logger.info("Decode string={}", new String(cb.array()));
		}
		logger.info("CharBuffer size={}, byte[] size/limit={}/{}, ByteBuffer remaining/position={}/{}, result={}", charSize, byteSize, limit, bb.remaining(), 
				bb.position(), res);
		limit=byteSize;
		cb=CharBuffer.allocate(charSize);
		res=css.decode(cb, bb, limit==byteSize);
		if (cb.hasArray()) {
			logger.info("Decode string={}", new String(cb.array()));
		}
		logger.info("CharBuffer size={}, byte[] size/limit={}/{}, ByteBuffer remaining/position={}/{}, result={}", charSize, byteSize, limit, bb.remaining(), 
				bb.position(), res);

	}
	/**​
	 * Convert array of bytes to array of hexadecimal representation of byte.​
	 * For example, byte[-1, 4] will be converted to ​
	 * @param in​
	 * @return​
	 */
	public static String[] toHex(byte[] in) {
		Byte[] ino = new Byte[in.length];
		Arrays.setAll(ino, n -> in[n]);
		return Stream.of(ino).map(bo -> "0x" + new String(new char[] { Character.forDigit((bo >> 4) & 0xF, 16),
				Character.forDigit(bo & 0xF, 16) }).toUpperCase()).collect(Collectors.toList()).toArray(new String[] {});
	}
	
	public static String toString(String[] strings) {
		return Arrays.toString(strings);
	}
}

	
