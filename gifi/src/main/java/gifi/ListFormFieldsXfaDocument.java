package gifi;

import java.io.IOException;
import java.util.Objects;

import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfEncodings;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfReader;

public class ListFormFieldsXfaDocument {
	
	private static final String pdfFilename="t1178-fill-18e - filled.pdf";
	public static void main(String[] args) throws IOException {
		PdfReader reader = new PdfReader(pdfFilename);
//		reader.getAcroFields().getFields().forEach((k, v) -> System.out.println(k+" value "+v.getValue(0)));;
//		reader.getAcroFields().getFields().entrySet().stream().map(es -> es.getValue()).map(i -> i.getValue(0)).map(d -> d.getKeys()).forEach(k -> System.out.println(k));
		reader.getAcroFields().getFields().entrySet().stream().map(entry -> entry.getValue())
			.forEach(item -> {
				PdfDictionary dictionary = item.getValue(0);
				StringBuffer sb=new StringBuffer();
				PdfObject val = dictionary.get(PdfName.T);
				if (Objects.nonNull(val)) {
					PdfEncodings.convertToString(val.getBytes(), PdfObject.TEXT_UNICODE);
					sb.append("/T="+PdfEncodings.convertToString(val.getBytes(), PdfObject.TEXT_UNICODE));
				}
				val = dictionary.get(PdfName.V);
				if (Objects.nonNull(val)) {
					sb.append(" ").append("/V="+val);
				}
				System.out.println(sb.toString());
			});
//			.map(item -> item.getValue(0))
//			.map(d -> d.getKeys()).forEach(k -> System.out.println(k));
	}
}
