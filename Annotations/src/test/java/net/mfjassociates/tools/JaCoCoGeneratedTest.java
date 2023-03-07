package net.mfjassociates.tools;

import static net.mfjassociates.tools.JaCoCoGenerated.JACOCO_GENERATED_CLASS_NAME;
import static net.mfjassociates.tools.JaCoCoGenerated.createCustomPackageRuntimeGeneratedAnnotation;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.StringWriter;
import java.util.Collection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFormatter;

public class JaCoCoGeneratedTest {
	
	private JCodeModel codeModel;
	private static final String generatedPackage="test.jacoco";
	private static final String FULLY_QUALIFIED_CLASS_NAME=generatedPackage+"."+JACOCO_GENERATED_CLASS_NAME;
	
	@BeforeEach
	public void setup() {
		codeModel=new JCodeModel();
	}
	
	@Test
	public void testMultipleInvocations() {
		createCustomPackageRuntimeGeneratedAnnotation(codeModel._package(generatedPackage));
		createCustomPackageRuntimeGeneratedAnnotation(codeModel._package(generatedPackage));
		JDefinedClass clazz = codeModel._getClass(FULLY_QUALIFIED_CLASS_NAME);
		Collection<JAnnotationUse> annotations = clazz.annotations();
		assertThat(annotations.size()).isEqualTo(3);
	}
	@Test
	public void testCreateCustomPackageRuntimeGeneratedAnnotation() {
		
		createCustomPackageRuntimeGeneratedAnnotation(codeModel._package(generatedPackage));
		JDefinedClass clazz = codeModel._getClass(FULLY_QUALIFIED_CLASS_NAME);
		assertThat(clazz).isNotNull().returns(true, JDefinedClass::isAnnotationTypeDeclaration);
		Collection<JAnnotationUse> annotations = clazz.annotations();
		assertThat(annotations.size()).isEqualTo(3);
		assertThat(annotations).anySatisfy(ja -> { // JAnnotationUse
			assertThat(ja.getAnnotationClass().name()).isEqualTo("Retention");
			assertThat(ja.getAnnotationMembers()).isNotNull();
			final StringWriter w=new StringWriter();
			final JFormatter jf=new JFormatter(w);
			ja.generate(jf);
			assertThat(w.getBuffer().toString()).contains("RetentionPolicy.CLASS");
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
	}

}
