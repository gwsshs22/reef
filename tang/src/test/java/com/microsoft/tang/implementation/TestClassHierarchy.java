package com.microsoft.tang.implementation;

import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.microsoft.tang.ClassHierarchy;
import com.microsoft.tang.Tang;
import com.microsoft.tang.annotations.Name;
import com.microsoft.tang.annotations.NamedParameter;
import com.microsoft.tang.annotations.Parameter;
import com.microsoft.tang.annotations.Unit;
import com.microsoft.tang.exceptions.ClassHierarchyException;
import com.microsoft.tang.exceptions.InjectionException;
import com.microsoft.tang.exceptions.NameResolutionException;
import com.microsoft.tang.types.ClassNode;
import com.microsoft.tang.types.ConstructorDef;
import com.microsoft.tang.types.Node;
import com.microsoft.tang.util.ReflectionUtilities;

public class TestClassHierarchy {
  public ClassHierarchy ns;

  @Rule public ExpectedException thrown = ExpectedException.none();
  
  @Before
  public void setUp() throws Exception {
    TangImpl.reset();
    ns = Tang.Factory.getTang().getDefaultClassHierarchy();
  }

  public String s(Class<?> c) {
    return ReflectionUtilities.getFullName(c);
  }
  
  @Test
  public void testJavaString() throws NameResolutionException {
    ns.getNode(ReflectionUtilities.getFullName(String.class));
    Node n = null;
    try {
      n = ns.getNode("java");
    } catch(NameResolutionException e) {
    }
    Assert.assertNull(n);
    try {
      n = ns.getNode("java.lang");
    } catch(NameResolutionException e) {
    }
    Assert.assertNull(n);
    Assert.assertNotNull(ns.getNode("java.lang.String"));
    try {
      ns.getNode("com.microsoft");
      Assert.fail("Didn't get expected exception");
    } catch (NameResolutionException e) {

    }
  }

  @Test
  public void testSimpleConstructors() throws NameResolutionException {
    ClassNode<?> cls = (ClassNode<?>) ns.getNode(s(SimpleConstructors.class));
    Assert.assertTrue(cls.getChildren().size() == 0);
    ConstructorDef<?> def[] = cls.getInjectableConstructors();
    Assert.assertEquals(3, def.length);
  }

  @Test
  public void testNamedParameterConstructors() throws NameResolutionException {
    ns.getNode(s(NamedParameterConstructors.class));
  }

  @Test
  public void testArray() throws NameResolutionException {
    thrown.expect(UnsupportedOperationException.class);
    thrown.expectMessage("No support for arrays, etc.  Name was: [Ljava.lang.String;");
    ns.getNode(s(new String[0].getClass()));
  }

  @Test
  public void testRepeatConstructorArg() throws NameResolutionException {
    thrown.expect(ClassHierarchyException.class);
    thrown.expectMessage("Repeated constructor parameter detected.  Cannot inject constructor com.microsoft.tang.implementation.RepeatConstructorArg(int,int)");
    ns.getNode(s(RepeatConstructorArg.class));
  }

  @Test
  public void testRepeatConstructorArgClasses() throws NameResolutionException {
    thrown.expect(ClassHierarchyException.class);
    thrown.expectMessage("Repeated constructor parameter detected.  Cannot inject constructor com.microsoft.tang.implementation.RepeatConstructorArgClasses(com.microsoft.tang.implementation.A,com.microsoft.tang.implementation.A)");
    ns.getNode(s(RepeatConstructorArgClasses.class));
  }

  @Test
  public void testLeafRepeatedConstructorArgClasses() throws NameResolutionException {
    ns.getNode(s(LeafRepeatedConstructorArgClasses.class));
  }

  @Test
  public void testNamedRepeatConstructorArgClasses() throws NameResolutionException {
    ns.getNode(s(NamedRepeatConstructorArgClasses.class));
  }

  @Test
  public void testResolveDependencies() throws NameResolutionException {
    ns.getNode(s(SimpleConstructors.class));
    Assert.assertNotNull(ns.getNode(ReflectionUtilities
        .getFullName(String.class)));
  }

  @Test
  public void testDocumentedLocalNamedParameter() throws NameResolutionException {
    ns.getNode(s(DocumentedLocalNamedParameter.class));
  }

  @Test
  public void testNamedParameterTypeMismatch() throws NameResolutionException {
    thrown.expect(ClassHierarchyException.class);
    thrown.expectMessage("Named parameter type mismatch.  Constructor expects a java.lang.String but Foo is a java.lang.Integer");
    ns.getNode(s(NamedParameterTypeMismatch.class));
  }

  @Test
  public void testUnannotatedName() throws NameResolutionException {
    thrown.expect(ClassHierarchyException.class);
    thrown.expectMessage("Named parameter com.microsoft.tang.implementation.UnannotatedName is missing its @NamedParameter annotation.");
    ns.getNode(s(UnannotatedName.class));
  }

  // TODO: The next three error messages should be more specific about the underlying cause of the failure.
  @Test
  public void testAnnotatedNotName() throws NameResolutionException {
    thrown.expect(ClassHierarchyException.class);
    thrown.expectMessage("Found illegal @NamedParameter com.microsoft.tang.implementation.AnnotatedNotName does not implement Name<?>");
    ns.getNode(s(AnnotatedNotName.class));
  }

  @Test
  public void testAnnotatedNameWrongInterface() throws NameResolutionException {
    thrown.expect(ClassHierarchyException.class);
    thrown.expectMessage("Found illegal @NamedParameter com.microsoft.tang.implementation.AnnotatedNameWrongInterface does not implement Name<?>");
    ns.getNode(s(AnnotatedNameWrongInterface.class));
  }

  @Test
  public void testAnnotatedNameNotGenericInterface() throws NameResolutionException {
    thrown.expect(ClassHierarchyException.class);
    thrown.expectMessage("Found illegal @NamedParameter com.microsoft.tang.implementation.AnnotatedNameNotGenericInterface does not implement Name<?>");
    ns.getNode(s(AnnotatedNameNotGenericInterface.class));
  }

  @Test
  public void testAnnotatedNameMultipleInterfaces() throws NameResolutionException {
    thrown.expect(ClassHierarchyException.class);
    thrown.expectMessage("Named parameter com.microsoft.tang.implementation.AnnotatedNameMultipleInterfaces implements multiple interfaces.  It is only allowed to implement Name<T>");
    ns.getNode(s(AnnotatedNameMultipleInterfaces.class));
  }

  @Test
  public void testUnAnnotatedNameMultipleInterfaces() throws NameResolutionException {
    thrown.expect(ClassHierarchyException.class);
    thrown.expectMessage("Named parameter com.microsoft.tang.implementation.UnAnnotatedNameMultipleInterfaces is missing its @NamedParameter annotation.");
    ns.getNode(s(UnAnnotatedNameMultipleInterfaces.class));
  }

  @Test
  public void testNameWithConstructor() throws NameResolutionException {
    thrown.expect(ClassHierarchyException.class);
    thrown.expectMessage("Named parameter com.microsoft.tang.implementation.NameWithConstructor has a constructor.  Named parameters must not declare any constructors.");
    ns.getNode(s(NameWithConstructor.class));
  }

  @Test
  public void testNameWithZeroArgInject() throws NameResolutionException {
    thrown.expect(ClassHierarchyException.class);
    thrown.expectMessage("Named parameter com.microsoft.tang.implementation.NameWithZeroArgInject has an injectable constructor.  Named parameters must not declare any constructors.");
    ns.getNode(s(NameWithZeroArgInject.class));
  }

  @Test
  public void testGenericTorture1() throws NameResolutionException {
    ns.getNode(s(GenericTorture1.class));
  }

  @Test
  public void testGenericTorture2() throws NameResolutionException {
    ns.getNode(s(GenericTorture2.class));
  }

  @Test
  public void testGenericTorture3() throws NameResolutionException {
    ns.getNode(s(GenericTorture3.class));
  }

  @Test
  public void testGenericTorture4() throws NameResolutionException {
    ns.getNode(s(GenericTorture4.class));
  }

  @Test
  public void testGenericTorture5() throws NameResolutionException {
    ns.getNode(s(GenericTorture5.class));
  }

  @Test
  public void testGenericTorture6() throws NameResolutionException {
    ns.getNode(s(GenericTorture6.class));
  }

  @Test
  public void testGenericTorture7() throws NameResolutionException {
    ns.getNode(s(GenericTorture7.class));
  }

  @Test
  public void testGenericTorture8() throws NameResolutionException {
    ns.getNode(s(GenericTorture8.class));
  }

  @Test
  public void testGenericTorture9() throws NameResolutionException {
    ns.getNode(s(GenericTorture9.class));
  }

  @Test
  public void testInjectNonStaticLocalArgClass() throws NameResolutionException {
    ns.getNode(s(InjectNonStaticLocalArgClass.class));
  }

  @Test(expected = ClassHierarchyException.class)
  public void testInjectNonStaticLocalType() throws NameResolutionException {
    ns.getNode(s(InjectNonStaticLocalType.class));
  }

  @Test(expected = ClassHierarchyException.class)
  public void testConflictingShortNames() throws NameResolutionException {
    ns.getNode(s(ShortNameFooA.class));
    ns.getNode(s(ShortNameFooB.class));
  }

  @Test
  public void testOKShortNames() throws NameResolutionException {
    ns.getNode(s(ShortNameFooA.class));
  }

  @Test
  public void testRoundTripInnerClassNames() throws ClassNotFoundException, NameResolutionException {
    Node n = ns.getNode(s(Nested.Inner.class));
    Class.forName(n.getFullName());
  }

  @Test
  public void testRoundTripAnonInnerClassNames() throws ClassNotFoundException, NameResolutionException {
    Node n = ns.getNode(s(AnonNested.x.getClass()));
    Node m = ns.getNode(s(AnonNested.y.getClass()));
    Assert.assertNotSame(n.getFullName(), m.getFullName());
    Class<?> c = Class.forName(n.getFullName());
    Class<?> d = Class.forName(m.getFullName());
    Assert.assertNotSame(c, d);
  }

  @Test
  public void testUnitIsInjectable() throws InjectionException, NameResolutionException {
    ClassNode<?> n = (ClassNode<?>) ns.getNode(s(OuterUnitTH.class));
    Assert.assertTrue(n.isUnit());
    Assert.assertTrue(n.isInjectionCandidate());
  }

  @Test
  public void testBadUnitDecl() throws NameResolutionException {
    thrown.expect(ClassHierarchyException.class);
    thrown.expectMessage("Detected explicit constructor in class enclosed in @Unit com.microsoft.tang.implementation.OuterUnitBad$InA  Such constructors are disallowed.");

    ns.getNode(s(OuterUnitBad.class));
  }

}

class SimpleConstructors {
  @Inject
  public SimpleConstructors() {
  }

  @Inject
  public SimpleConstructors(int x) {
  }

  public SimpleConstructors(String x) {
  }

  @Inject
  public SimpleConstructors(int x, String y) {
  }
}

class NamedParameterConstructors {
  @NamedParameter()
  class X implements Name<String> {
  };

  @Inject
  public NamedParameterConstructors(String x, @Parameter(X.class) String y) {
  }
}

class RepeatConstructorArg {
  public @Inject
  RepeatConstructorArg(int x, int y) {
  }
}

class A {
}

class RepeatConstructorArgClasses {
  public @Inject
  RepeatConstructorArgClasses(A x, A y) {
  }
}

class LeafRepeatedConstructorArgClasses {
  static class A {
    class AA {
    }
  }

  static class B {
    class AA {
    }
  }

  static class C {
    @Inject
    C(A.AA a, B.AA b) {
    }
  }
}

@NamedParameter()
class AA implements Name<A> {
}

@NamedParameter()
class BB implements Name<A> {
}

class NamedRepeatConstructorArgClasses {
  public @Inject
  NamedRepeatConstructorArgClasses(@Parameter(AA.class) A x,
      @Parameter(BB.class) A y) {
  }
}

class DocumentedLocalNamedParameter {
  @NamedParameter(doc = "doc stuff", default_value = "some value")
  final class Foo implements Name<String> {
  }

  @Inject
  public DocumentedLocalNamedParameter(@Parameter(Foo.class) String s) {
  }
}

class NamedParameterTypeMismatch {
  @NamedParameter(doc = "doc.stuff", default_value = "1")
  final class Foo implements Name<Integer> {
  }

  @Inject
  public NamedParameterTypeMismatch(@Parameter(Foo.class) String s) {
  }
}

class UnannotatedName implements Name<String> {
}

interface I1 {
}

@NamedParameter(doc = "c")
class AnnotatedNotName {
}

@NamedParameter(doc = "c")
class AnnotatedNameWrongInterface implements I1 {
}

@SuppressWarnings("rawtypes")
@NamedParameter(doc = "c")
class AnnotatedNameNotGenericInterface implements Name {
}

class UnAnnotatedNameMultipleInterfaces implements Name<Object>, I1 {
}

@NamedParameter(doc = "c")
class AnnotatedNameMultipleInterfaces implements Name<Object>, I1 {
}

@NamedParameter()
class NameWithConstructor implements Name<Object> {
  private NameWithConstructor(int i) {
  }
}

@NamedParameter()
class NameWithZeroArgInject implements Name<Object> {
  @Inject
  public NameWithZeroArgInject() {
  }
}

@NamedParameter()
class GenericTorture1 implements Name<Set<String>> {
}

@NamedParameter()
class GenericTorture2 implements Name<Set<?>> {
}

@NamedParameter()
class GenericTorture3 implements Name<Set<Set<String>>> {
}

@SuppressWarnings("rawtypes")
@NamedParameter()
class GenericTorture4 implements Name<Set<Set>> {
}

@NamedParameter()
class GenericTorture5 implements Name<Map<Set<String>, Set<String>>> {
}

@SuppressWarnings("rawtypes")
@NamedParameter()
class GenericTorture6 implements Name<Map<Set, Set<String>>> {
}

@SuppressWarnings("rawtypes")
@NamedParameter()
class GenericTorture7 implements Name<Map<Set<String>, Set>> {
}

@NamedParameter()
class GenericTorture8 implements Name<Map<String, String>> {
}

@SuppressWarnings("rawtypes")
@NamedParameter()
class GenericTorture9 implements Name<Map<Set, Set>> {
}

class InjectNonStaticLocalArgClass {
  class NonStaticLocal {
  }

  @Inject
  InjectNonStaticLocalArgClass(NonStaticLocal x) {
  }
}

class InjectNonStaticLocalType {
  class NonStaticLocal {
    @Inject
    NonStaticLocal(NonStaticLocal x) {
    }
  }
}

@NamedParameter(short_name = "foo")
class ShortNameFooA implements Name<String> {
}

@NamedParameter(short_name = "foo")
class ShortNameFooB implements Name<Integer> {
}

class Nested {
  class Inner {
  }
}

class AnonNested {
  static interface X {
  }

  static X x = new X() {
    @SuppressWarnings("unused")
    int i;
  };
  static X y = new X() {
    @SuppressWarnings("unused")
    int j;
  };
}

@Unit
class OuterUnitBad {
  class InA {
    @Inject
    InA() {
    }
  }

  class InB {
  }
}

@Unit
class OuterUnitTH {
  class InA {
  }

  class InB {
  }
}
