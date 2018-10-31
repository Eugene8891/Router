package com.midrive.voice.router_compiler.processor;

import com.google.auto.service.AutoService;
import com.midrive.voice.router_annotation.annotation.Intercept;
import com.midrive.voice.router_compiler.utils.Logger;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import org.omg.PortableInterceptor.Interceptor;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import static com.midrive.voice.router_compiler.utils.Const.ANNO_TYPE_INTERCEPTOR;
import static com.midrive.voice.router_compiler.utils.Const.INTERCEPTOR;
import static com.midrive.voice.router_compiler.utils.Const.INTERCEPTOR_GROUP;
import static com.midrive.voice.router_compiler.utils.Const.METHOD_LOAD;
import static com.midrive.voice.router_compiler.utils.Const.MODULE_NAME;
import static com.midrive.voice.router_compiler.utils.Const.NAME_OF_INTERCEPTOR;
import static com.midrive.voice.router_compiler.utils.Const.PACKAGE_OF_GENERATE_FILE;
import static com.midrive.voice.router_compiler.utils.Const.SEPARATOR;
import static com.midrive.voice.router_compiler.utils.Const.WARNING_TIPS;
import static javax.lang.model.element.Modifier.PUBLIC;

/**
 * Process the annotation
 */
@AutoService(Processor.class)
@SupportedOptions(MODULE_NAME)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes(ANNO_TYPE_INTERCEPTOR)
public class InterceptorProcessor extends AbstractProcessor {
    private Map<Integer, Element> interceptors = new TreeMap<>();
    private Filer mFiler;       // File util, write class file into disk.
    private Elements elementUtil;
    private String moduleName = null;   // Module name, maybe its 'app' or others
    private TypeMirror tmInterceptor = null;
    private Logger logger;
    private static final String TAG = "InterceptorProcessor: ";
    /**
     * Initializes the processor with the processing environment by
     * setting the {@code processingEnv} field to the value of the
     * {@code processingEnv} argument.  An {@code
     * IllegalStateException} will be thrown if this method is called
     * more than once on the same object.
     *
     * @param processingEnv environment to access facilities the tool framework
     *                      provides to the processor
     * @throws IllegalStateException if this method is called more than once.
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        mFiler = processingEnv.getFiler();                  // Generate class.
        elementUtil = processingEnv.getElementUtils();      // Get class meta.
        logger = new Logger(processingEnv.getMessager());   // Package the log utils.

        // Attempt to get user configuration [moduleName]
        Map<String, String> options = processingEnv.getOptions();
        if (options != null && !options.isEmpty()) {
            moduleName = options.get(MODULE_NAME);
        }

        tmInterceptor = elementUtil.getTypeElement(INTERCEPTOR).asType();

        logger.info(">>> InterceptorProcessor init. <<<");
    }

    /**
     * {@inheritDoc}
     *
     * @param annotations
     * @param roundEnv
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations != null && annotations.size() > 0) {
            Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Intercept.class);
            try {
                parseInterceptors(elements);
            } catch (Exception e) {
                logger.error(e);
            }
            return true;
        }

        return false;
    }

    /**
     * Parse tollgate.
     *
     * @param elements elements of tollgate.
     */
    private void parseInterceptors(Set<? extends Element> elements) throws IOException {
        if (elements != null && elements.size() > 0) {
            logger.info(TAG+">>> Found interceptors, size is " + elements.size() + " <<<");

            // Verify and cache, sort incidentally.
            for (Element element : elements) {
                if (verify(element)) {  // Check the interceptor meta
                    logger.info(TAG+">>> A interceptor verify over, its " + element.asType()+ " <<<");
                    Intercept interceptor = element.getAnnotation(Intercept.class);

                    Element lastInterceptor = interceptors.get(interceptor.priority());
                    if (null != lastInterceptor) { // Added, throw exceptions
                        throw new IllegalArgumentException(
                                String.format(Locale.getDefault(), "More than one interceptors use same priority [%d], They are [%s] and [%s].",
                                        interceptor.priority(),
                                        lastInterceptor.getSimpleName(),
                                        element.getSimpleName())
                        );
                    }

                    interceptors.put(interceptor.priority(), element);
                } else {
                    logger.error(TAG+"A interceptor verify failed, it does not implement Intercept, its " + element.asType());
                }
            }

            // Interface of Router.
            TypeElement type_interceptor = elementUtil.getTypeElement(INTERCEPTOR);
            TypeElement type_interceptorGroup = elementUtil.getTypeElement(INTERCEPTOR_GROUP);

            /**
             *  Build input type, format as :
             *
             *  Map<Integer, Class<? extends Intercept>>
             */
            ParameterizedTypeName inputMapTypeOfInterceptor = ParameterizedTypeName.get(
                    ClassName.get(Map.class),
                    ClassName.get(Integer.class),
                    ParameterizedTypeName.get(
                            ClassName.get(Class.class),
                            WildcardTypeName.subtypeOf(ClassName.get(type_interceptor))
                    )
            );

            // Build input param name.
            ParameterSpec interceptorParamSpec = ParameterSpec.builder(inputMapTypeOfInterceptor, "interceptors").build();

            // Build method : 'load'
            MethodSpec.Builder loadMethodOfInterceptorBuilder = MethodSpec.methodBuilder(METHOD_LOAD)
                    .addAnnotation(Override.class)
                    .addModifiers(PUBLIC)
                    .addParameter(interceptorParamSpec);

            // Generate
            if (null != interceptors && interceptors.size() > 0) {
                // Build method body
                for (Map.Entry<Integer, Element> entry : interceptors.entrySet()) {
                    loadMethodOfInterceptorBuilder.addStatement("interceptors.put(" + entry.getKey() + ", $T.class)", ClassName.get((TypeElement) entry.getValue()));
                }
            }
            logger.info(TAG+"=== Generated interceptor: " + moduleName + "===");
            // Write to disk(Write file even interceptors is empty.)
            JavaFile.builder(PACKAGE_OF_GENERATE_FILE,
                    TypeSpec.classBuilder(NAME_OF_INTERCEPTOR + SEPARATOR + moduleName)
                            .addModifiers(PUBLIC)
                            .addJavadoc(WARNING_TIPS)
                            .addMethod(loadMethodOfInterceptorBuilder.build())
                            .addSuperinterface(ClassName.get(type_interceptorGroup))
                            .build()
            ).build().writeTo(mFiler);

            logger.info(TAG+">>> Intercept group write over. <<<");
        }
    }

    /**
     * Verify inteceptor meta
     *
     * @param element Intercept taw type
     * @return verify result
     */
    private boolean verify(Element element) {
        Intercept intercept = element.getAnnotation(Intercept.class);
        // It must be implement the interface IInterceptor and marked with annotation Intercept.
        return null != intercept && ((TypeElement) element).getInterfaces().contains(tmInterceptor);
    }
}
