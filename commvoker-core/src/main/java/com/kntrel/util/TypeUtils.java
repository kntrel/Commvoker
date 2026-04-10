package com.kntrel.util;

import java.lang.reflect.*;
import java.util.*;

/** Reflection utilities for checking "B safely extends A". */
/**
 * Internal API. Public for framework wiring, but not part of the supported external API.
 */
public final class TypeUtils {

    private static final Map<Class<?>, Class<?>> PRIMITIVE_WRAPPERS = Map.of(
        boolean.class, Boolean.class,
        byte.class, Byte.class,
        char.class, Character.class,
        short.class, Short.class,
        int.class, Integer.class,
        long.class, Long.class,
        float.class, Float.class,
        double.class, Double.class,
        void.class, Void.class
    );


    public static boolean isAssignableFrom(Class<?> A, Class<?> B) {
        if (A.isPrimitive()) {
            A = PRIMITIVE_WRAPPERS.getOrDefault(A, A);
        }
        if (B.isPrimitive()) {
            B = PRIMITIVE_WRAPPERS.getOrDefault(B, B);
        }
        return A.isAssignableFrom(B);
    }

    public static Class<?> boxed(Class<?> c) {
        if (c.isPrimitive()) {
            return PRIMITIVE_WRAPPERS.getOrDefault(c, c);
        }
        return c;
    }

    /** Main entry: returns true iff B is a subtype of A. */
    public static boolean isSubtypeOf(Type B, Type A) {
        if (A == null || B == null) return false;
        if (A.equals(B)) return true;

        // Handle wildcards on A
        if (A instanceof WildcardType w) {
            // ? extends U1 & U2 & ...
            for (Type ub : w.getUpperBounds()) {
                if (!isSubtypeOf(B, ub)) return false;
            }
            // ? super L1 & L2 & ...
            for (Type lb : w.getLowerBounds()) {
                if (!isSubtypeOf(lb, B)) return false; // B must be above all lowers
            }
            return true;
        }

        // Handle type variables on A
        if (A instanceof TypeVariable) {
            for (Type ub : ((TypeVariable<?>) A).getBounds()) {
                if (!isSubtypeOf(B, ub)) return false;
            }
            return true;
        }

        // Handle arrays
        if (isArray(A) && isArray(B)) {
            return isSubtypeOf(arrayComponent(B), arrayComponent(A));
        }
        if (isArray(A)) {
            return false;
        }

        // At this point A is either Class or ParameterizedType
        // Handle A is raw Class
        if (A instanceof Class<?> aRaw && !aRaw.isArray()) {
            Class<?> bRaw = getRawClass(B);
            return bRaw != null && TypeUtils.isAssignableFrom(aRaw, bRaw);
        }

        // Handle A is a ParameterizedType
        if (A instanceof ParameterizedType aPT) {
            Class<?> aRaw = (Class<?>) aPT.getRawType();

            // Find B's view of A's raw type, resolving type variables on the path from B to A
            ParameterizedType bViewOfA = viewAs(B, aRaw);
            if (bViewOfA == null) return false;

            Type[] aArgs = aPT.getActualTypeArguments();
            Type[] bArgs = bViewOfA.getActualTypeArguments();
            TypeVariable<?>[] aParams = aRaw.getTypeParameters();

            for (int i = 0; i < aArgs.length; i++) {
                Type aArg = aArgs[i];
                Type bArg = bArgs[i];

                // In Java generics, type arguments are invariant unless A uses wildcards.
                if (aArg instanceof WildcardType) {
                    if (!matchWildcard((WildcardType) aArg, bArg)) return false;
                } else {
                    // Concrete aArg => require equality after substitution
                    if (!equalTypes(aArg, bArg)) return false;
                }
            }
            return true;
        }

        // Shouldn't reach here, but be safe:
        return false;
    }

    /* ---------- helpers ---------- */

    private static boolean matchWildcard(WildcardType aWildcard, Type bArg) {
        // Upper bounds: bArg must be a subtype of every upper bound
        for (Type ub : aWildcard.getUpperBounds()) {
            if (!isSubtypeOf(bArg, ub)) return false;
        }
        // Lower bounds: bArg must be a supertype of every lower bound
        for (Type lb : aWildcard.getLowerBounds()) {
            if (!isSubtypeOf(lb, bArg)) return false;
        }
        return true;
    }

    private static boolean equalTypes(Type x, Type y) {
        if (x.equals(y)) return true;
        // Treat Class<?> vs ParameterizedType with no params as unequal always.
        // Handle arrays: compare component types
        if (isArray(x) && isArray(y)) {
            return equalTypes(arrayComponent(x), arrayComponent(y));
        }
        return false;
    }

    private static boolean isArray(Type t) {
        if (t instanceof Class) return ((Class<?>) t).isArray();
        return t instanceof GenericArrayType;
    }

    private static Type arrayComponent(Type t) {
        if (t instanceof Class) return ((Class<?>) t).getComponentType();
        return ((GenericArrayType) t).getGenericComponentType();
    }

    /** Returns the raw class for any Type, or null if it can't be determined. */
    private static Class<?> getRawClass(Type t) {
        if (t instanceof Class<?> c) { return c; }
        if (t instanceof ParameterizedType pt) { return (Class<?>) pt.getRawType(); }
        if (t instanceof GenericArrayType a) {
            Type c = a.getGenericComponentType();
            Class<?> rc = getRawClass(c);
            return (rc == null) ? null : Array.newInstance(rc, 0).getClass();
        }
        if (t instanceof TypeVariable<?> tv) {
            // Use the first upper bound (or Object) as a raw approximation
            Type[] bs = tv.getBounds();
            return (bs.length == 0) ? Object.class : getRawClass(bs[0]);
        }
        if (t instanceof WildcardType wt) {
            Type[] bs = wt.getUpperBounds();
            return (bs.length == 0) ? Object.class : getRawClass(bs[0]);
        }
        return null;
    }

    /**
     * Produce a ParameterizedType view of `B` as `targetRaw`, resolving type variables along the path.
     * Returns null if B is not assignable to targetRaw.
     */
    private static ParameterizedType viewAs(Type B, Class<?> targetRaw) {
        // BFS up the type graph while carrying a TypeVariable->Type substitution map.
        Deque<Node> q = new ArrayDeque<>();
        q.add(new Node(B, new HashMap<>()));

        while (!q.isEmpty()) {
            Node n = q.removeFirst();
            Type t = n.t;

            if (t instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) t;
                Class<?> raw = (Class<?>) pt.getRawType();
                Map<TypeVariable<?>, Type> map = extendMap(n.subst, raw, pt.getActualTypeArguments());

                if (raw.equals(targetRaw)) {
                    // Build a concrete PT for targetRaw by applying map to its type params
                    TypeVariable<?>[] params = raw.getTypeParameters();
                    Type[] args = new Type[params.length];
                    for (int i = 0; i < params.length; i++) {
                        args[i] = apply(map, params[i]);
                    }
                    return new SimplePT(raw, args, raw.getDeclaringClass());
                }
                enqueueSupers(raw, map, q);
            } else if (t instanceof Class) {
                Class<?> c = (Class<?>) t;
                if (c.equals(targetRaw)) {
                    // Treat raw as PT with its type params unresolved == Object (invariantly unsafe),
                    // so we return a PT with the class’s own type variables (not substituted) => caller
                    // will require exact matches (usually false unless A uses wildcards).
                    TypeVariable<?>[] params = c.getTypeParameters();
                    Type[] args = new Type[params.length];
                    for (int i = 0; i < params.length; i++) args[i] = params[i];
                    return new SimplePT(c, args, c.getDeclaringClass());
                }
                // No actual args at this node; propagate current map
                enqueueSupers(c, n.subst, q);
            } else if (t instanceof GenericArrayType || (t instanceof Class && ((Class<?>) t).isArray())) {
                // Arrays: only Object[], Cloneable, Serializable, and Object are supertypes;
                // fall through to raw assignability coverage by enqueueSupers if any.
                Class<?> raw = getRawClass(t);
                if (raw != null) enqueueSupers(raw, n.subst, q);
            } else if (t instanceof TypeVariable || t instanceof WildcardType) {
                // Expand via upper bounds
                for (Type ub : upperBoundsOf(t)) q.add(new Node(apply(n.subst, ub), n.subst));
            }
        }
        return null;
    }

    private static void enqueueSupers(Class<?> raw, Map<TypeVariable<?>, Type> map, Deque<Node> q) {
        Type gs = raw.getGenericSuperclass();
        if (gs != null) q.add(new Node(apply(map, gs), new HashMap<>(map)));
        for (Type gi : raw.getGenericInterfaces()) {
            q.add(new Node(apply(map, gi), new HashMap<>(map)));
        }
    }

    private static List<Type> upperBoundsOf(Type t) {
        if (t instanceof TypeVariable) return Arrays.asList(((TypeVariable<?>) t).getBounds());
        if (t instanceof WildcardType) return Arrays.asList(((WildcardType) t).getUpperBounds());
        return Collections.singletonList(Object.class);
    }

    private static Map<TypeVariable<?>, Type> extendMap(Map<TypeVariable<?>, Type> parent, Class<?> raw, Type[] actuals) {
        Map<TypeVariable<?>, Type> out = new HashMap<>(parent);
        TypeVariable<?>[] params = raw.getTypeParameters();
        for (int i = 0; i < params.length && i < actuals.length; i++) {
            out.put(params[i], apply(parent, actuals[i]));
        }
        return out;
    }

    /** Substitute type variables in t using map (deep). */
    private static Type apply(Map<TypeVariable<?>, Type> map, Type t) {
        if (t instanceof TypeVariable) {
            Type r = map.get((TypeVariable<?>) t);
            return (r == null) ? t : apply(map, r);
        }
        if (t instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) t;
            Type[] args = pt.getActualTypeArguments();
            Type[] na = new Type[args.length];
            for (int i = 0; i < args.length; i++) na[i] = apply(map, args[i]);
            return new SimplePT((Class<?>) pt.getRawType(), na, pt.getOwnerType());
        }
        if (t instanceof WildcardType) {
            WildcardType w = (WildcardType) t;
            Type[] ub = w.getUpperBounds(), lb = w.getLowerBounds();
            for (int i = 0; i < ub.length; i++) ub[i] = apply(map, ub[i]);
            for (int i = 0; i < lb.length; i++) lb[i] = apply(map, lb[i]);
            return new SimpleWC(ub, lb);
        }
        if (t instanceof GenericArrayType) {
            return new SimpleGA(apply(map, ((GenericArrayType) t).getGenericComponentType()));
        }
        if (t instanceof Class<?> c && c.isArray()) {
            Class<?> ac = c.getComponentType();
            return new SimpleGA(apply(map, ac));
        }
        return t;
    }

    /* Tiny implementations to create Types at runtime */

    public static final class SimplePT implements ParameterizedType {
        private final Class<?> raw; private final Type[] args; private final Type owner;
        public SimplePT(Class<?> raw, Type[] args, Type owner) { this.raw = raw; this.args = args.clone(); this.owner = owner; }
        @Override public Type[] getActualTypeArguments() { return args.clone(); }
        @Override public Type getRawType() { return raw; }
        @Override public Type getOwnerType() { return owner; }
        @Override public boolean equals(Object o){ return o instanceof ParameterizedType p &&
                Objects.equals(raw,p.getRawType()) && Arrays.equals(args,p.getActualTypeArguments()) &&
                Objects.equals(owner,p.getOwnerType()); }
        @Override public int hashCode(){ return Objects.hash(raw, Arrays.hashCode(args), owner); }
        @Override public String toString(){ return raw.getTypeName()+ "<" + String.join(",", Arrays.stream(args).map(Type::getTypeName).toArray(String[]::new)) + ">"; }
    }

    private static final class SimpleWC implements WildcardType {
        private final Type[] ub, lb;
        SimpleWC(Type[] ub, Type[] lb){ this.ub = ub.clone(); this.lb = lb.clone(); }
        @Override public Type[] getUpperBounds(){ return ub.clone(); }
        @Override public Type[] getLowerBounds(){ return lb.clone(); }
        @Override public String toString(){ String u = Arrays.toString(ub); String l = Arrays.toString(lb); return "? extends " + u + " super " + l; }
    }

    private static final class SimpleGA implements GenericArrayType {
        private final Type c; SimpleGA(Type c){ this.c = c; }
        @Override public Type getGenericComponentType(){ return c; }
        @Override public String toString(){ return c.getTypeName() + "[]"; }
    }

    private static final class Node {
        final Type t; final Map<TypeVariable<?>, Type> subst;
        Node(Type t, Map<TypeVariable<?>, Type> m){ this.t = t; this.subst = m; }
    }
}

