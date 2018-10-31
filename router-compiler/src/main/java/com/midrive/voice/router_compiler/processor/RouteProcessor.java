package com.midrive.voice.router_compiler.processor;

import com.google.auto.service.AutoService;
import com.midrive.voice.router_annotation.annotation.Route;
import com.midrive.voice.router_annotation.model.RouteMeta;
import com.midrive.voice.router_annotation.model.RouteType;
import com.midrive.voice.router_compiler.utils.Const;
import com.midrive.voice.router_compiler.utils.Logger;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

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
import javax.lang.model.util.Types;
import static com.midrive.voice.router_compiler.utils.Const.ACTIVITY;
import static com.midrive.voice.router_compiler.utils.Const.ANNO_TYPE_ROUTE;
import static com.midrive.voice.router_compiler.utils.Const.FRAGMENT;
import static com.midrive.voice.router_compiler.utils.Const.FRAGMENT_V4;
import static com.midrive.voice.router_compiler.utils.Const.METHOD_LOAD;
import static com.midrive.voice.router_compiler.utils.Const.MODULE_NAME;
import static com.midrive.voice.router_compiler.utils.Const.NAME_OF_GROUP;
import static com.midrive.voice.router_compiler.utils.Const.NAME_OF_PROVIDER;
import static com.midrive.voice.router_compiler.utils.Const.NAME_OF_ROOT;
import static com.midrive.voice.router_compiler.utils.Const.PACKAGE_OF_GENERATE_FILE;
import static com.midrive.voice.router_compiler.utils.Const.PROVIDER_GROUP;
import static com.midrive.voice.router_compiler.utils.Const.ROUTE_GROUP;
import static com.midrive.voice.router_compiler.utils.Const.ROUTE_ROOT;
import static com.midrive.voice.router_compiler.utils.Const.SEPARATOR;
import static com.midrive.voice.router_compiler.utils.Const.SERVICE;
import static com.midrive.voice.router_compiler.utils.Const.WARNING_TIPS;
import static javax.lang.model.element.Modifier.PROTECTED;
import static javax.lang.model.element.Modifier.PUBLIC;

/**
 * A processor used to find route.
 */
@AutoService(Processor.class)
@SupportedOptions({MODULE_NAME})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes({ANNO_TYPE_ROUTE})
public class RouteProcessor extends AbstractProcessor {
    private Map<String, Set<RouteMeta>> groups = new HashMap<>(); // GroupName and routeMeta.
    private Map<String, String> roots = new TreeMap<>();
    private Filer mFiler;       // File util, write class file into disk.
    private Types types;
    private Elements elements;
    //private String moduleName = null;   // Module name, maybe its 'app' or others
    private TypeMirror provider = null;
    private Logger logger;
    private String moduleName;
    private static final String TAG = "RouteProcessor: ";

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mFiler = processingEnv.getFiler();               // Generate class.
        types = processingEnv.getTypeUtils();            // Get type utils.
        elements = processingEnv.getElementUtils();      // Get class meta.

        provider = elements.getTypeElement(Const.PROVIDER).asType();
        Map<String, String> options = processingEnv.getOptions();
        if(options != null && options.size() > 0) {
            moduleName = options.get(MODULE_NAME);
        }
        if(moduleName != null && "" != moduleName) {
            moduleName = moduleName.replaceAll("[^0-9a-zA-Z_]+", "");
        }
        logger = new Logger(processingEnv.getMessager());
        logger.info("=== RouteProcessor inited. ===");
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
        if (annotations != null && annotations.size() > 0) {
            Set<? extends Element> routeElements = roundEnvironment.getElementsAnnotatedWith(Route.class);
            try {
                logger.info(TAG+"=== Found routes, start... ===");
                this.parseRoutes(routeElements);

            } catch (Exception e) {
                logger.error(e);
            }
            return true;
        }
        return false;
    }

    private void parseRoutes(Set<? extends Element> routeElements) throws IOException {
        if(routeElements != null && routeElements.size() > 0) {
            roots.clear();

            TypeMirror type_Activity = elements.getTypeElement(ACTIVITY).asType();
            TypeMirror type_Service = elements.getTypeElement(SERVICE).asType();
            TypeMirror fragmentTm = elements.getTypeElement(FRAGMENT).asType();
            TypeMirror fragmentTmV4 = elements.getTypeElement(FRAGMENT_V4).asType();

            ClassName routeMetaCn = ClassName.get(RouteMeta.class);
            ClassName routeTypeCn = ClassName.get(RouteType.class);
            TypeElement type_providerGroup = elements.getTypeElement(PROVIDER_GROUP);
            TypeElement type_routeGroup = elements.getTypeElement(ROUTE_GROUP);
            TypeElement type_routeRoot = elements.getTypeElement(ROUTE_ROOT);

            for (Element element : routeElements) {
                TypeMirror tm = element.asType();
                Route route = element.getAnnotation(Route.class);
                RouteMeta routeMeta;

                if (types.isSubtype(tm, type_Activity)) {         // Activity
                    logger.info(TAG+"=== Found activity route: " + tm.toString() + " ===");
                    routeMeta = new RouteMeta(route, element, RouteType.ACTIVITY, null);
                } else if (types.isSubtype(tm, provider)) {         // IProvider
                    logger.info(TAG+"=== Found provider route: " + tm.toString() + " ===");
                    routeMeta = new RouteMeta(route, element, RouteType.PROVIDER, null);
                } else if (types.isSubtype(tm, type_Service)) {           // Service
                    logger.info(TAG+"=== Found service route: " + tm.toString() + " ===");
                    routeMeta = new RouteMeta(route, element, RouteType.parse(SERVICE), null);
                } else if (types.isSubtype(tm, fragmentTm) || types.isSubtype(tm, fragmentTmV4)) {
                    logger.info(TAG+"=== Found fragment route: " + tm.toString() + " ===");
                    routeMeta = new RouteMeta(route, element, RouteType.parse(FRAGMENT), null);
                } else {
                    throw new RuntimeException("Router::Compiler === Found unsupported class type, type = [" + types.toString() + "].");
                }
                categories(routeMeta);
            }

            /*
              Map<String, RouteMeta>
             */
            ParameterizedTypeName inputMapTypeOfGroup = ParameterizedTypeName.get(
                    ClassName.get(Map.class),
                    ClassName.get(String.class),
                    ClassName.get(RouteMeta.class)
            );

            /*
              Build input param name.
             */
            ParameterSpec groupParamSpec = ParameterSpec.builder(inputMapTypeOfGroup, "routes").build();
            ParameterSpec providerParamSpec = ParameterSpec.builder(inputMapTypeOfGroup, "providers").build();
            /*
              Build ProviderGroup method : 'load'
             */
            MethodSpec.Builder loadMethodOfProviderBuilder = MethodSpec.methodBuilder(METHOD_LOAD)
                    .addAnnotation(Override.class)
                    .addModifiers(PUBLIC)
                    .addParameter(providerParamSpec);

            // Start generate java source.
            for (Map.Entry<String, Set<RouteMeta>> entry : groups.entrySet()) {
                String groupName = entry.getKey();

                MethodSpec.Builder loadMethodOfGroupBuilder = MethodSpec.methodBuilder(METHOD_LOAD)
                        .addAnnotation(Override.class)
                        .addModifiers(PUBLIC)
                        .addParameter(groupParamSpec);

                // Build group method body
                Set<RouteMeta> groupRoute = entry.getValue();
                for (RouteMeta routeMeta : groupRoute) {
                    ClassName className = ClassName.get((TypeElement) routeMeta.getRawType());

                    switch (routeMeta.getType()) {
                        case PROVIDER:  // Need cache provider's super class
                            List<? extends TypeMirror> interfaces = ((TypeElement)routeMeta.getRawType()).getInterfaces();
                            for (TypeMirror tm : interfaces) {
                                if (types.isSameType(tm, provider)) {   // Its implements provider interface himself.
                                    // This interface extend the IProvider, so it can be used for mark provider
                                    loadMethodOfProviderBuilder.addStatement(
                                            "providers.put($S, $T.build($T." + routeMeta.getType() + ", $T.class, $S, $S, null, " + routeMeta.getPriority() + ", " + routeMeta.getExtra() + "))",
                                            (routeMeta.getRawType()).toString(),
                                            routeMetaCn,
                                            routeTypeCn,
                                            className,
                                            routeMeta.getPath(),
                                            routeMeta.getGroup());
                                } else if (types.isSubtype(tm, provider)) {
                                    // This interface extend the IProvider, so it can be used for mark provider
                                    loadMethodOfProviderBuilder.addStatement(
                                            "providers.put($S, $T.build($T." + routeMeta.getType() + ", $T.class, $S, $S, null, " + routeMeta.getPriority() + ", " + routeMeta.getExtra() + "))",
                                            tm.toString(),    // So stupid, will duplicate only save class name.
                                            routeMetaCn,
                                            routeTypeCn,
                                            className,
                                            routeMeta.getPath(),
                                            routeMeta.getGroup());
                                    logger.info(TAG+"providers: "+tm.toString());
                                }
                            }
                            break;
                        default:
                            break;
                    }

                    loadMethodOfGroupBuilder.addStatement(
                            "routes.put($S, $T.build($T." + routeMeta.getType() + ", $T.class, $S, $S, " + null + ", " + routeMeta.getPriority() + ", " + routeMeta.getExtra() + "))",
                            routeMeta.getPath(),
                            routeMetaCn,
                            routeTypeCn,
                            className,
                            routeMeta.getPath().toLowerCase(),
                            routeMeta.getGroup().toLowerCase());
                }

                // Generate groups
                String groupFileName = NAME_OF_GROUP + groupName;
                JavaFile.builder(PACKAGE_OF_GENERATE_FILE,
                        TypeSpec.classBuilder(groupFileName)
                                .addJavadoc(WARNING_TIPS)
                                .addSuperinterface(ClassName.get(type_routeGroup))
                                .addModifiers(PUBLIC)
                                .addMethod(loadMethodOfGroupBuilder.build())
                                .build()
                ).build().writeTo(mFiler);
                logger.info(TAG+"=== Generated group: " + groupFileName+"===");

                roots.put(groupName, groupFileName);
            }

            // Write provider file
            String providerFileName = NAME_OF_PROVIDER + SEPARATOR + moduleName;
            JavaFile.builder(PACKAGE_OF_GENERATE_FILE,
                    TypeSpec.classBuilder(providerFileName)
                            .addJavadoc(WARNING_TIPS)
                            .addSuperinterface(ClassName.get(type_providerGroup))
                            .addModifiers(PUBLIC)
                            .addMethod(loadMethodOfProviderBuilder.build())
                            .build()
            ).build().writeTo(mFiler);

            logger.info(TAG+"=== Generated provider map, name is " + providerFileName + " ===");

            // Write root file.
            /*
               Build input type, format is :

               Map<String, Class<? extends Group>>
             */
            ParameterizedTypeName inputMapTypeOfRoot = ParameterizedTypeName.get(
                    ClassName.get(Map.class),
                    ClassName.get(String.class),
                    ParameterizedTypeName.get(
                            ClassName.get(Class.class),
                            WildcardTypeName.subtypeOf(ClassName.get(type_routeGroup))
                    )
            );
            ParameterSpec rootParamSpec = ParameterSpec.builder(inputMapTypeOfRoot, "groups").build();
            /*
              Build RouteRoot method : 'load'
             */
            MethodSpec.Builder loadMethodOfRootBuilder = MethodSpec.methodBuilder(METHOD_LOAD)
                    .addAnnotation(Override.class)
                    .addModifiers(PUBLIC)
                    .addParameter(rootParamSpec);
            if (roots.size() > 0) {
                // Generate root meta by group name, it must be generated before root, then I can find out the class of group.
                for (Map.Entry<String, String> entry : roots.entrySet()) {
                    loadMethodOfRootBuilder.addStatement("groups.put($S, $T.class)", entry.getKey(), ClassName.get(PACKAGE_OF_GENERATE_FILE, entry.getValue()));
                }
            }
            String rootFileName = NAME_OF_ROOT + SEPARATOR + moduleName;
            JavaFile.builder(PACKAGE_OF_GENERATE_FILE,
                    TypeSpec.classBuilder(rootFileName)
                            .addJavadoc(WARNING_TIPS)
                            .addSuperinterface(ClassName.get(elements.getTypeElement(ROUTE_ROOT)))
                            .addModifiers(PUBLIC)
                            .addMethod(loadMethodOfRootBuilder.build())
                            .build()
            ).build().writeTo(mFiler);

            logger.info(TAG+"=== Generated root, name is " + rootFileName + " ===");
        }
    }

    /**
     * Sort metas in group.
     *
     * @param routeMeta metas.
     */
    private void categories(RouteMeta routeMeta) {
        if (verifyRoute(routeMeta)) {
            logger.info(TAG+"=== Start categories, group = " + routeMeta.getGroup() + ", path = " + routeMeta.getPath() + " ===");
            Set<RouteMeta> routeMetas = groups.get(routeMeta.getGroup());
            if (routeMetas == null) {
                Set<RouteMeta> routeMetaSet = new TreeSet<>(new Comparator<RouteMeta>() {
                    @Override
                    public int compare(RouteMeta r1, RouteMeta r2) {
                        try {
                            return r1.getPath().compareTo(r2.getPath());
                        } catch (NullPointerException npe) {
                            logger.error(npe.getMessage());
                            return 0;
                        }
                    }
                });
                routeMetaSet.add(routeMeta);
                groups.put(routeMeta.getGroup(), routeMetaSet);
            } else {
                routeMetas.add(routeMeta);
            }
        } else {
            logger.warning(TAG+"=== Route meta verify error, group is " + routeMeta.getGroup() + " ===");
        }
    }

    /**
     * Verify the route meta
     *
     * @param meta raw meta
     */
    private boolean verifyRoute(RouteMeta meta) {
        String path = meta.getPath();

        if (path == null || !path.startsWith("/")) {   // The path must be start with '/' and not empty!
            return false;
        }
        if (meta.getGroup() != null) { // Use default group(the first word in path)
            try {
                String defaultGroup = path.substring(1, path.indexOf("/", 1));
                if (defaultGroup == null || "" == defaultGroup) {
                    return false;
                }
                meta.setGroup(defaultGroup);
                return true;
            } catch (Exception e) {
                logger.error("Failed to extract default group! " + e.getMessage());
                return false;
            }
        }
        return true;
    }
}
