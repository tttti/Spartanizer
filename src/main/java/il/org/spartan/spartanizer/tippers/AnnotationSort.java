package il.org.spartan.spartanizer.tippers;

import java.util.*;
import java.util.stream.*;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.*;
import org.eclipse.text.edits.*;

import il.org.spartan.*;
import il.org.spartan.spartanizer.ast.navigate.*;
import il.org.spartan.spartanizer.ast.safety.*;
import il.org.spartan.spartanizer.dispatch.*;
import il.org.spartan.spartanizer.engine.*;
import il.org.spartan.spartanizer.tipping.*;

/** @author kobybs
 * @author Dan Abramovich
 * @since 20-11-2016 */
public class AnnotationSort<N extends BodyDeclaration> extends EagerTipper<N> implements TipperCategory.Sorting {
  private static final HashSet<String> rankTable[] = as.array(//
      new HashSet<>(as.list("Deprecated")), //
      new HashSet<>(as.list("Override")), //
      new HashSet<>(as.list("Documented", "FunctionalInterface", "Inherited", "Retention", "Repeatable", "SafeVarargs", "Target")),
      new HashSet<>(as.list("$USER_DEFINED_ANNOTATION$")),
      new HashSet<>(as.list("Action", "Addressing", "BindingType", "ConstructorProperties", "DescriptorKey", "FaultAction", "Generated",
          "HandlerChain", "InitParam", "MTOM", "MXBean", "Oneway", "PostConstruct", "PreDestroy", "RequestWrapper", "Resource", "Resources",
          "RespectBinding", "ResponseWrapper", "ServiceMode", "SOAPBinding", "SOAPMessageHandler", "SOAPMessageHandlers", "SupportedAnnotationTypes",
          "SupportedOptions", "SupportedSourceVersion", "Transient", "WebEndpoint", "WebFault", "WebMethod", "WebParam", "WebResult", "WebService",
          "WebServiceClient", "WebServiceFeatureAnnotation", "WebServiceProvider", "WebServiceRef", "WebServiceRefs", "XmlAccessorOrder",
          "XmlAccessorType", "XmlAnyAttribute", "XmlAnyElement", "XmlAttachmentRef", "XmlAttribute", "XmlElement", "XmlElementDecl", "XmlElementRef",
          "XmlElementRefs", "XmlElements", "XmlElementWrapper", "XmlEnum", "XmlEnumValue", "XmlID", "XmlIDREF", "XmlInlineBinaryData",
          "XmlJavaTypeAdapter", "XmlJavaTypeAdapters", "XmlList", "XmlMimeType", "XmlMixed", "XmlNs", "XmlRegistry", "XmlRootElement", "XmlSchema",
          "XmlSchemaType", "XmlSchemaTypes", "XmlSeeAlso", "XmlTransient", "XmlType", "XmlValue")),
      new HashSet<>(as.list("SuppressWarnings")), //
      new HashSet<>(as.list("NonNull", "Nullable")));

  public static int rankAnnotation(final IExtendedModifier ¢) {
    return rankAnnotation(az.annotation(¢).getTypeName().getFullyQualifiedName());
  }

  public static int rankAnnotation(final String annotationName) {
    int $ = 0;
    for (final HashSet<String> ¢ : rankTable) {
      ++$;
      if (¢.contains(annotationName))
        return $;
    }
    return rankAnnotation("$USER_DEFINED_ANNOTATION$");
  }

  static final Comparator<IExtendedModifier> comp = (m1, m2) -> rankAnnotation(m1) - rankAnnotation(m2) == 0 ? (m1 + "").compareTo(m2 + "")
      : rankAnnotation(m1) - rankAnnotation(m2);

  public static int compare(final String annotation1, final String annotation2) {
    return rankAnnotation(annotation1) - rankAnnotation(annotation2) == 0 ? annotation1.compareTo(annotation2)
        : rankAnnotation(annotation1) - rankAnnotation(annotation2);
  }

  private static List<? extends IExtendedModifier> sort(final List<? extends IExtendedModifier> ¢) {
    return ¢.stream().sorted(comp).collect(Collectors.toList());
  }

  @Override public Tip tip(final N n) {
    if (n == null || az.bodyDeclaration(n) == null)
      return null;
    final List<Annotation> $ = extract.annotations(n);
    if ($ == null || $.isEmpty())
      return null;
    final List<Annotation> myCopy = new ArrayList<>();
    myCopy.addAll($);
    Collections.sort(myCopy, comp);
    return myCopy.equals($) ? null : new Tip(description(n), n, this.getClass()) {
      @Override public void go(final ASTRewrite r, final TextEditGroup g) {
        final ListRewrite l = r.getListRewrite(n, n.getModifiersProperty());
        for (int i = 0; i < $.size(); ++i) {
          final ASTNode oldNode = $.get(i);
          final ASTNode newNode = myCopy.get(i);
          if (oldNode != newNode)
            l.replace(oldNode, r.createMoveTarget(newNode), g);
        }
      }
    };
  }

  @Override public String description(final N ¢) {
    return "Sort annotations of " + extract.category(¢) + " " + extract.name(¢) + " (" + extract.annotations(¢) + "->" + sort(extract.annotations(¢))
        + ")";
  }

  public static final class ofAnnotation extends AnnotationSort<AnnotationTypeDeclaration> { //
  }

  public static final class ofAnnotationTypeMember extends AnnotationSort<AnnotationTypeMemberDeclaration> { //
  }

  public static final class ofEnum extends AnnotationSort<EnumDeclaration> { //
  }

  public static final class ofEnumConstant extends AnnotationSort<EnumConstantDeclaration> { //
  }

  public static final class ofField extends AnnotationSort<FieldDeclaration> { //
  }

  public static final class ofInitializer extends AnnotationSort<Initializer> { //
  }

  public static final class ofMethod extends AnnotationSort<MethodDeclaration> { //
  }

  public static final class ofType extends AnnotationSort<TypeDeclaration> { //
  }
}