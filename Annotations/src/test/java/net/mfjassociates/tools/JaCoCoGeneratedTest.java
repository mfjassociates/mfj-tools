package net.mfjassociates.tools;

import static net.mfjassociates.tools.JaCoCoGenerated.JACOCO_GENERATED_CLASS_NAME;
import static net.mfjassociates.tools.JaCoCoGenerated.createCustomPackageRuntimeGeneratedAnnotation;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.StringWriter;
import java.lang.annotation.RetentionPolicy;
import java.util.Collection;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JAnnotationValue;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFormatter;

public class JaCoCoGeneratedTest {
	
	private JCodeModel codeModel;
	private String generatedPackage="test.jacoco";
	
	@BeforeEach
	public void setup() {
		codeModel=new JCodeModel();
	}
	
	@Test
	public void testCreateCustomPackageRuntimeGeneratedAnnotation() {
		
		createCustomPackageRuntimeGeneratedAnnotation(codeModel._package(generatedPackage));
		String fullyQualifiedClassName=generatedPackage+"."+JACOCO_GENERATED_CLASS_NAME;
		JDefinedClass clazz = codeModel._getClass(fullyQualifiedClassName);
		assertThat(clazz).isNotNull().returns(true, JDefinedClass::isAnnotationTypeDeclaration);
		Collection<JAnnotationUse> annotations = clazz.annotations();
		assertThat(annotations).anySatisfy(ja -> { // JAnnotationUse
			assertThat(ja.getAnnotationClass().name()).isEqualTo("Retention");
			assertThat(ja.getAnnotationMembers()).isNotNull();
			final StringWriter w=new StringWriter();
			final JFormatter jf=new JFormatter(w);
			ja.generate(jf);
			assertThat(w.getBuffer().toString()).contains("RetentionPolicy.RUNTIME");
		});
		assertThat(annotations).anySatisfy(ja -> { // JAnnotationUse
			assertThat(ja.getAnnotationClass().name()).isEqualTo("Target");
			assertThat(ja.getAnnotationMembers()).isNotNull();
			final StringWriter w=new StringWriter();
			final JFormatter jf=new JFormatter(w);
			ja.generate(jf);
			assertThat(w.getBuffer().toString()).contains("ElementType.TYPE");
			assertThat(w.getBuffer().toString()).contains("ElementType.METHOD");
			assertThat(w.getBuffer().toString()).contains("ElementType.CONSTRUCTOR");
		});
		assertThat(annotations).anySatisfy(ja -> {
			assertThat(ja.getAnnotationClass().name()).isEqualTo("Documented");
			assertThrows(NullPointerException.class, () -> {
				assertThat(ja.getAnnotationMembers()).isNull();
			}, "Documented annotation had annotation members and should not have any"); 
		});
//		.anySatisfy(ja -> {
//			assertThat(ja.getAnnotationClass().name()).isEqualTo("Retention");
//			assertThat(ja.getAnnotationMembers()).extracting(mv -> mv.get("value")).isEqualTo(RetentionPolicy.RUNTIME);
//		});
//		assertThat(annotations).anySatisfy(ja -> {
//			assertThat(ja.getAnnotationClass().name()).isEqualTo("Documented");
//			assertThat(ja.getAnnotationMembers()).isNull();
//		});
//		assertThat(annotations).extracting(JAnnotationUse::getAnnotationClass).contains((JClass)null);
//		assertThat(annotations).anySatisfy(ja -> {
//			assertThat(ja.getAnnotationClass().name()).isEqualTo("Documented");
//			//.extracting("value","aa").containsExactly(JExpr.lit("adsaf"));
//		});
	}

}
