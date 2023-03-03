package net.mfjassociates.tools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.UnmappableCharacterException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is thread safe.
 * @author MXJ037
 *
 */
public class CharsetStreamSupport {

	public static final Logger logger=LoggerFactory.getLogger(CharsetStreamSupport.class);
	private final ThreadLocal<String> charsetName=new ThreadLocal<String>();
	private ThreadLocal<CharsetDecoder> csDecoder=new ThreadLocal<CharsetDecoder>();
	private ThreadLocal<ByteBuffer> bbinternal=new ThreadLocal<ByteBuffer>();
	private final ThreadLocal<Charset> charset=new ThreadLocal<Charset>();
	private final ThreadLocal<CoderResult> result=new ThreadLocal<CoderResult>();

	public CoderResult getCoderReult() {
		return result.get();
	}
	public Charset getCharset() {
		return charset.get();
	}
	private final ThreadLocal<Boolean> decoderInitialized=new ThreadLocal<Boolean>() {
		@Override protected Boolean initialValue() {
			return false;
		}
	};
	public void initialize(String aCharsetName, int capacity) {
		charsetName.set(aCharsetName);
		bbinternal.set(ByteBuffer.allocate(capacity));
		charset.set(Charset.forName(charsetName.get()));
		csDecoder.set(charset.get().newDecoder());
	}
	
	public CharsetStreamSupport() {
	}
	public CoderResult decode2(ByteBuffer bb, CharBuffer cb, boolean eoi) {
		if (!decoderInitialized.get()) {
			csDecoder.get().reset();
			decoderInitialized.set(true);
		}
		CoderResult res = csDecoder.get().decode(bb, cb, eoi);
		if (eoi) {
			csDecoder.get().flush(cb);
			cb.flip();
			decoderInitialized.set(false);
		}
		return res;
	}
	public CharBuffer decode(byte[] bytes, boolean eoi) {
		return decode(ByteBuffer.wrap(bytes), eoi);
	}
	/**
	 * Will call decode2 with a CharBuffer allocated to
	 * approximately the right size for receiving the decoded characters.
	 * 
	 * @param inbb - The input byte buffer to decode
	 * @param eoi - end of input processing
	 * @return - the decoded character buffer
	 */
	public CharBuffer decode(ByteBuffer inbb, boolean eoi) {
		if (bbinternal.get()==null) throw new IllegalStateException("You must initialize the CharsetStreamSupport before calling decode2");
		ByteBuffer bb = bbinternal.get();
		bb.put(inbb);
		((Buffer)bb).flip();
		CharBuffer cb=CharBuffer.allocate((int)(bb.capacity()*averageCharsPerByte()));
		
		result.set(decode2(bb, cb, eoi));
		if (!eoi) cb.flip();
		bb.compact();
		return cb;
	}
	public static void main7(String[] args) {
		CharsetStreamSupport css = new CharsetStreamSupport();
		css.initialize("UTF-8", 10);
		byte[] utf8bytes=new String("élèves").getBytes(css.getCharset());
		for (int ix = 0; ix < utf8bytes.length+1; ix++) {
			byte[] part1=Arrays.copyOf(utf8bytes, ix);
			byte[] part2=Arrays.copyOfRange(utf8bytes, ix, utf8bytes.length);
			CharBuffer cb;
			cb = css.decode(part1, false);
			CoderResult res1=css.getCoderReult();
			String decoded1=cb.toString();
			cb = css.decode(part2, true);
			System.out.println(MessageFormat.format("decoded={0}/{1} result={2}/{3}", decoded1,cb.toString(),res1, css.getCoderReult()));
		}
	}
	public static void main6(String[] args) {
			CharsetStreamSupport css = new CharsetStreamSupport();
			css.initialize("UTF-8", 10);
			byte[] utf8bytes=new String("élèves").getBytes(css.getCharset());
			byte[] part1=Arrays.copyOf(utf8bytes, 4);
			byte[] part2=Arrays.copyOfRange(utf8bytes, 4, utf8bytes.length);
			CoderResult res=null;
			System.out.println("part1="+toString(toHex(part1))+" part2="+toString(toHex(part2)));
			System.out.println("utf8bytes="+toString(toHex(utf8bytes)));
			
			ByteBuffer bb=ByteBuffer.allocate(utf8bytes.length);
			CharBuffer cb=CharBuffer.allocate((int)(bb.capacity()*css.averageCharsPerByte()));
			
			bb.put(part1);
			bb.flip();
			res=css.decode2(bb, cb, false);
			
			cb.flip();
			System.out.println("decoded="+cb.toString());
			cb.clear();
	
			
	//		System.out.println("before compact "+extendedDisplayBuffer(bb, "bb"));
			bb.compact();
	//		System.out.println("after compact "+extendedDisplayBuffer(bb, "bb"));
			
			bb.put(part2);
			bb.flip();
			
			res=css.decode2(bb, cb, true);
			System.out.println("decoded="+cb.toString());
			
		}
	public static void main3(String[] args) throws UnsupportedEncodingException, CharacterCodingException {
		byte[] a=new byte[] {(byte) 0x80,(byte) 0x31,(byte) 0x80,(byte) 0x32,(byte) 0x80,(byte) 0x33};
		String ts="€1€2€3";
		ts="élè€ve";
		final Charset cs=Charset.forName("ISO-8859-1");
		CharsetEncoder encoder = cs.newEncoder();
		encoder.reset();
		CharBuffer ochars=CharBuffer.wrap(ts);
		System.out.println(displayBuffer(ochars, "ochars"));
		ochars.rewind();
		ByteBuffer ret = null;
		try {
			ret = encoder.encode(ochars);
		} catch (UnmappableCharacterException e) {
			throw new IllegalArgumentException(MessageFormat.format("The character ''{0}'' at position {1} could not be mapped", ochars.get(ochars.position()), ochars.position()), e);
		}
		byte[] encoded=ret.array();
		System.out.println("encoded="+Arrays.toString((encoded)));
		System.out.println("encoded="+toString(toHex(encoded)));
		System.out.println("encoded="+toBinary(encoded));
		System.out.println("a="+Arrays.toString((a)));
		System.out.println("a="+toBinary(a));
		System.out.println(new String(encoded,"ISO-8859-1"));
	}
	private static final String FORMAT="{0},\"{1}\",{2},{3},\"{4}\"";
	private static final String FORMAT2="{},\"{}\",{},{}";

	/**
	 * main2
	 * This is from this stackoverflow:
	 * https://stackoverflow.com/a/29560129
	 * This shows how to call decode with false as end of input argument
	 */
	private static void main2(String[] args) throws IOException {
		final String pound="€1€2€3";
		final Charset charset=Charset.forName("ISO-8859-1");
		final byte[] tab = pound.getBytes(charset); //char €
	    final int maxi=tab.length-1;
		final CharsetDecoder dec = charset.newDecoder();
		final int charsLen=(int)(dec.maxCharsPerByte() * (double)tab.length);
		final CharBuffer chars = CharBuffer.allocate(charsLen);
		final PrintWriter csv=new PrintWriter(new FileWriter(new File("results.csv")));
		logger.info("byte[]={}, chars length={} maxCharsPerByte={}, maxi={}", toString(toHex(tab)), charsLen, dec.maxCharsPerByte(), maxi);
		logger.info("Buffer=position/remaining/limit/capacity");
		Stream.of(new String[]{"i","when","iposition","iremaining","ilimit","icapacity","oposition","oremaining","olimit","ocapacity","result"}).forEach(header -> csv.append('"').append(header).append('"').append(','));;
		csv.append("\n");

		final ByteBuffer buffer = ByteBuffer.allocate(tab.length);

		for (int i = 0; i < tab.length; i++) {
		    csv.println(MessageFormat.format(FORMAT, i, "before put", csvDisplayBuffer(buffer),csvDisplayBuffer(chars),""));
		    // Add the next byte to the buffer
		    buffer.put(tab[i]);

		    // Remember the current position
		    final int pos = buffer.position();
		    logger.info(FORMAT2, i, "after put", displayBuffer(buffer, "buffer"),displayBuffer(chars, "chars"));
		    csv.println(MessageFormat.format(FORMAT, i, "after put", csvDisplayBuffer(buffer),csvDisplayBuffer(chars),""));

		    // Try to decode
		    buffer.flip();
		    boolean lastLoop=i == maxi;
		    logger.info(FORMAT2, i, "after flip", displayBuffer(buffer, "buffer"),displayBuffer(chars, "chars"));
		    csv.println(MessageFormat.format(FORMAT, i, "after flip", csvDisplayBuffer(buffer),csvDisplayBuffer(chars),""));

		    final CoderResult result = dec.decode(buffer, chars, lastLoop);
		    logger.info(FORMAT2, i, "after decode", displayBuffer(buffer, "buffer"),displayBuffer(chars, "chars"));
		    csv.println(MessageFormat.format(FORMAT, i, "after decode", csvDisplayBuffer(buffer),csvDisplayBuffer(chars), result));
			logger.info("result={} lastLoop={} decoded=\"{}\"", result, lastLoop, displayBufferContent(chars));

		    if (result.isUnderflow()) {
		        // Underflow, prepare the buffer for more writing
		    	if (!lastLoop) chars.rewind();
		        buffer.limit(buffer.capacity());
		        buffer.position(pos);
		    }
		}

		dec.flush(chars);
		chars.flip();
		logger.info("decoded=\"{}\"", new String(chars.array()));
		logger.info("decoded=\"{}\"", new String(tab, charset));
		csv.close();
		
	}
	private static String displayBuffer(Buffer aBuffer, String name) {
		return MessageFormat.format("\"{0} p/r/l/c\",{1},{2},{3},{4}", name, aBuffer.position(), aBuffer.remaining(), aBuffer.limit(), aBuffer.capacity());
	}
	
	private static String csvDisplayBuffer(Buffer aBuffer) {
		return MessageFormat.format("{0},{1},{2},{3}", aBuffer.position(), aBuffer.remaining(), aBuffer.limit(), aBuffer.capacity());
	}
	
	private static String extendedDisplayBuffer(Buffer aBuffer, String name) {
		StringBuffer sb=new StringBuffer(3*aBuffer.capacity());
		sb.append(name).append(": ");
		if (aBuffer instanceof CharBuffer) {
			CharBuffer cb=(CharBuffer)aBuffer;
			sb.append(Arrays.toString(cb.array()));
		} else {
			ByteBuffer bb=(ByteBuffer)aBuffer;
			sb.append(toString(toHex(bb.array())));
		}
		sb.append("\n");
		sb.append("position=").append(aBuffer.position())
			.append(" remaining=").append(aBuffer.remaining())
			.append(" limit=").append(aBuffer.limit())
			.append(" capacity=").append(aBuffer.capacity())
			.append("\n");			
		return sb.toString();
	}
	
	public float averageCharsPerByte() {
		return csDecoder.get().averageCharsPerByte();
	}
	
	private static String displayBufferContent(Buffer aBuffer) {
		int limit=aBuffer.limit();
		int pos=aBuffer.position();
		int remaining=aBuffer.remaining();
		if (pos!=0) aBuffer.flip();
		String ret="";
		if (aBuffer instanceof CharBuffer) {
			CharBuffer cBuffer=(CharBuffer)aBuffer;
			ret=cBuffer.toString();
		} else {
			ret=aBuffer.toString();
		}
		aBuffer.limit(limit);
		aBuffer.position(pos);
		if (remaining!=aBuffer.remaining()) throw new IllegalStateException("Unable to restore buffer after displaying its contents");
		return ret;
	}
	/**
	 * main1
	 * @param args
	 */
	/**
	 * main1
	 * This illustrates the following sequence
	 * <p>
	 * Step 1: decode 4 bytes from byte[].  This should decode the first two characters because byte is required to
	 * decode the third character.  Output in cb. Position in bb is at the fourth (unused) byte.
	 * <p>
	 * Step 2: decode remaining byte 4 onwards to complete the character string.
	 * <p>
	 * Step 1 has eoi false, step 2 has eoi true.
	 * 
	 */
	private static void main1(String[] args) {
		int limit;
		int charSize;
		int byteSize;
		
		CharsetStreamSupport css = new CharsetStreamSupport();
		css.initialize("UTF-8", 10);
		byte[] utf8bytes=new String("élèves").getBytes(css.getCharset());
		byteSize=utf8bytes.length;
		logger.info(toString(toHex(utf8bytes)));
		charSize=(int)(((double)byteSize)*css.averageCharsPerByte());
		
		limit=byteSize;
		limit=4;
		
		logger.info("Buffer=position/remaining/limit/capacity");
		
		CharBuffer cb=CharBuffer.allocate(charSize);
		ByteBuffer bb=ByteBuffer.wrap(utf8bytes);
		
		bb.limit(limit);
		
		CoderResult res = css.decode2(bb, cb, limit==byteSize);
	    logger.info(FORMAT2, 1, "after decode1", displayBuffer(bb, "bb"),displayBuffer(cb, "cb"));
		if (cb.hasArray()) {
			logger.info("Decode string={}, eoi={}, result={}", displayBufferContent(cb), limit==byteSize, res);
		}
//		logger.info("CharBuffer size={}, byte[] size/limit={}/{}, ByteBuffer remaining/position={}/{}, result={}", charSize, byteSize, limit, bb.remaining(), 
//				bb.position(), res);
		
		limit=byteSize;
//		bb.rewind();
		bb.limit(limit);
//		cb.rewind();
		
	    logger.info(FORMAT2, 2, "before decode2", displayBuffer(bb, "bb"),displayBuffer(cb, "cb"));
		res=css.decode2(bb, cb, limit==byteSize);
	    logger.info(FORMAT2, 2, "after decode2", displayBuffer(bb, "bb"),displayBuffer(cb, "cb"));
		if (cb.hasArray()) {
			logger.info("Decode string={}, eoi={}, result={}", displayBufferContent(cb), limit==byteSize, res);
		}
//		logger.info("CharBuffer size={}, byte[] size/limit={}/{}, ByteBuffer remaining/position={}/{}, result={}", charSize, byteSize, limit, bb.remaining(), 
//				bb.position(), res);

	}
	private static void main5(String[] args) {
		int byteSize;
		
		CharsetStreamSupport css = new CharsetStreamSupport();
		css.initialize("UTF-8", 10);
		byte[] utf8bytes=new String("élèves").getBytes(css.getCharset());
		byteSize=utf8bytes.length;

		CharBuffer cb=CharBuffer.allocate((int)(((double)byteSize)*css.averageCharsPerByte()));
		ByteBuffer bb=ByteBuffer.wrap(utf8bytes);
		
		// step 1
		bb.limit(4);		
		CoderResult res = css.decode2(bb, cb, bb.limit()==byteSize);

		// step 2
		bb.limit(byteSize);
		res=css.decode2(bb, cb, bb.limit()==byteSize);

	}
	private static void main4(String[] args) throws IOException {
		CharsetStreamSupport css = new CharsetStreamSupport();
		css.initialize("UTF-8", 10);
		byte[] utf8bytes=new String("élèves").getBytes(css.getCharset());
		int byteSize=utf8bytes.length;
		logger.info(toString(toHex(utf8bytes)));
		int charSize=(int)(((double)byteSize)*css.averageCharsPerByte());
		ByteArrayInputStream bais=new ByteArrayInputStream(utf8bytes);
		ReadableByteChannel channel = Channels.newChannel(bais);
		ByteBuffer bb=ByteBuffer.wrap(utf8bytes);
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		channel.read(bb);
		
	}
	/**
	 * @param args - args to program
	 */
	public static void main(String[] args) {
		main7(args);
	}
	/**​
	 * Convert array of bytes to array of hexadecimal representation of byte.​
	 * For example, byte[-1, 4] will be converted to ​
	 */
	private static String[] toHex(byte[] in) {
		Byte[] ino = new Byte[in.length];
		Arrays.setAll(ino, n -> in[n]);
		return Stream.of(ino).map(bo -> "0x" + new String(new char[] { Character.forDigit((bo >> 4) & 0xF, 16),
				Character.forDigit(bo & 0xF, 16) }).toUpperCase()).collect(Collectors.toList()).toArray(new String[] {});
	}
	
	private static String toBinary(byte[] encoded) {
		return IntStream.range(0, encoded.length).map(i -> encoded[i])
				.mapToObj(e -> Integer.toBinaryString(e ^ 255)).map(e -> String.format("%1$" + Byte.SIZE + "s", e)
				.replace(" ", "0")).collect(Collectors.joining(" "));
	}
	
	private static String toString(String[] strings) {
		return Arrays.toString(strings);
	}
}

	
