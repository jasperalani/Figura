package org.moon.figura.math.matrix;

import com.mojang.math.Matrix3f;
import org.luaj.vm2.LuaError;
import org.lwjgl.BufferUtils;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.lua.LuaType;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.utils.caching.CacheStack;
import org.moon.figura.utils.caching.CacheUtils;

import java.nio.FloatBuffer;

@LuaType(typeName = "mat3")
public class FiguraMat3 extends FiguraMatrix<FiguraMat3, FiguraVec3> {

    private static final FloatBuffer copyingBuffer = BufferUtils.createFloatBuffer(3*3);
    public static FiguraMat3 fromMatrix3f(Matrix3f mat) {
        copyingBuffer.clear();
        mat.store(copyingBuffer);
        return of(copyingBuffer.get(), copyingBuffer.get(), copyingBuffer.get(),
                copyingBuffer.get(), copyingBuffer.get(), copyingBuffer.get(),
                copyingBuffer.get(), copyingBuffer.get(), copyingBuffer.get());
    }
    public Matrix3f toMatrix3f() {
        copyingBuffer.clear();
        copyingBuffer
                .put((float) v11).put((float) v21).put((float) v31)
                .put((float) v12).put((float) v22).put((float) v32)
                .put((float) v13).put((float) v23).put((float) v33);
        Matrix3f result = new Matrix3f();
        result.load(copyingBuffer);
        return result;
    }



    public void translate(double x, double y) {
        v11 += x * v31;
        v12 += x * v32;
        v13 += x * v33;

        v21 += y * v31;
        v22 += y * v32;
        v23 += y * v33;
        invalidate();
    }

    //Values are named as v(ROW)(COLUMN), both 1-indexed like in actual math
    public double v11, v12, v13, v21, v22, v23, v31, v32, v33;

    @Override
    public void resetIdentity() {
        v12=v13=v21=v23=v31=v32 = 0;
        v11=v22=v33 = 1;
    }
    @Override
    public CacheUtils.Cache<FiguraMat3> getCache() {
        return CACHE;
    }
    private static final CacheUtils.Cache<FiguraMat3> CACHE = CacheUtils.getCache(FiguraMat3::new, 100);
    public static FiguraMat3 of() {
        return CACHE.getFresh();
    }
    public static FiguraMat3 of(double n11, double n21, double n31,
                                double n12, double n22, double n32,
                                double n13, double n23, double n33) {
        return of().set(n11, n21, n31, n12, n22, n32, n13, n23, n33);
    }
    public static class Stack extends CacheStack<FiguraMat3, FiguraMat3> {
        public Stack() {
            this(CACHE);
        }
        public Stack(CacheUtils.Cache<FiguraMat3> cache) {
            super(cache);
        }
        @Override
        protected void modify(FiguraMat3 valueToModify, FiguraMat3 modifierArg) {
            valueToModify.rightMultiply(modifierArg);
        }
        @Override
        protected void copy(FiguraMat3 from, FiguraMat3 to) {
            to.set(from);
        }
    }

    @Override
    protected double calculateDeterminant() {
        double sub11 = v22 * v33 - v23 * v32;
        double sub12 = v21 * v33 - v23 * v31;
        double sub13 = v21 * v32 - v22 * v31;
        return v11 * sub11 - v12 * sub12 + v13 * sub13;
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload,
            description = "matrix_n.copy"
    )
    public FiguraMat3 copy() {
        return of(v11, v21, v31, v12, v22, v32, v13, v23, v33);
    }

    @Override
    public boolean equals(FiguraMat3 o) {
        return
                v11 == o.v11 && v12 == o.v12 && v13 == o.v13 &&
                v21 == o.v21 && v22 == o.v22 && v23 == o.v23 &&
                v31 == o.v31 && v32 == o.v32 && v33 == o.v33;
    }
    @Override
    public boolean equals(Object other) {
        if (other instanceof FiguraMat3 o)
            return equals(o);
        return false;
    }
    @Override
    public String toString() {
        return "\n[  " +
                (float) v11 + ", " + (float) v12 + ", " + (float) v13 + ", " +
                "\n   " + (float) v21 + ", " + v22 + ", " + (float) v23 + ", " +
                "\n   " + (float) v31 + ", " + (float) v32 + ", " + (float) v33 +
                "  ]";
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = Integer.class,
                    argumentNames = "col"
            ),
            description = "matrix_n.get_column"
    )
    public FiguraVec3 getColumn(int col) {
        return switch (col) {
            case 1 -> FiguraVec3.of(v11, v21, v31);
            case 2 -> FiguraVec3.of(v12, v22, v32);
            case 3 -> FiguraVec3.of(v13, v23, v33);
            default -> throw new LuaError("Column must be 1 to " + cols());
        };
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = Integer.class,
                    argumentNames = "row"
            ),
            description = "matrix_n.get_row"
    )
    public FiguraVec3 getRow(int row) {
        return switch (row) {
            case 1 -> FiguraVec3.of(v11, v12, v13);
            case 2 -> FiguraVec3.of(v21, v22, v23);
            case 3 -> FiguraVec3.of(v31, v32, v33);
            default -> throw new LuaError("Row must be 1 to " + rows());
        };
    }

    @Override
    public int rows() {
        return 3;
    }

    @Override
    public int cols() {
        return 3;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraMat3.class,
                    argumentNames = "other"
            ),
            description = "matrix_n.set"
    )
    public FiguraMat3 set(FiguraMat3 o) {
        return set(o.v11, o.v21, o.v31, o.v12, o.v22, o.v32, o.v13, o.v23, o.v33);
    }

    public FiguraMat3 set(double n11, double n21, double n31,
                          double n12, double n22, double n32,
                          double n13, double n23, double n33) {
        v11 = n11;
        v12 = n12;
        v13 = n13;
        v21 = n21;
        v22 = n22;
        v23 = n23;
        v31 = n31;
        v32 = n32;
        v33 = n33;
        invalidate();
        return this;
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraMat3.class,
                    argumentNames = "other"
            ),
            description = "matrix_n.multiply"
    )
    public FiguraMat3 multiply(FiguraMat3 o) {
        double nv11 = o.v11*v11+o.v12*v21+o.v13*v31;
        double nv12 = o.v11*v12+o.v12*v22+o.v13*v32;
        double nv13 = o.v11*v13+o.v12*v23+o.v13*v33;

        double nv21 = o.v21*v11+o.v22*v21+o.v23*v31;
        double nv22 = o.v21*v12+o.v22*v22+o.v23*v32;
        double nv23 = o.v21*v13+o.v22*v23+o.v23*v33;

        double nv31 = o.v31*v11+o.v32*v21+o.v33*v31;
        double nv32 = o.v31*v12+o.v32*v22+o.v33*v32;
        double nv33 = o.v31*v13+o.v32*v23+o.v33*v33;

        v11 = nv11;
        v12 = nv12;
        v13 = nv13;
        v21 = nv21;
        v22 = nv22;
        v23 = nv23;
        v31 = nv31;
        v32 = nv32;
        v33 = nv33;
        invalidate();
        return this;
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraMat3.class,
                    argumentNames = "other"
            ),
            description = "matrix_n.right_multiply"
    )
    public FiguraMat3 rightMultiply(FiguraMat3 o) {
        double nv11 = v11*o.v11+v12*o.v21+v13*o.v31;
        double nv12 = v11*o.v12+v12*o.v22+v13*o.v32;
        double nv13 = v11*o.v13+v12*o.v23+v13*o.v33;

        double nv21 = v21*o.v11+v22*o.v21+v23*o.v31;
        double nv22 = v21*o.v12+v22*o.v22+v23*o.v32;
        double nv23 = v21*o.v13+v22*o.v23+v23*o.v33;

        double nv31 = v31*o.v11+v32*o.v21+v33*o.v31;
        double nv32 = v31*o.v12+v32*o.v22+v33*o.v32;
        double nv33 = v31*o.v13+v32*o.v23+v33*o.v33;

        v11 = nv11;
        v12 = nv12;
        v13 = nv13;
        v21 = nv21;
        v22 = nv22;
        v23 = nv23;
        v31 = nv31;
        v32 = nv32;
        v33 = nv33;
        invalidate();
        return this;
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload,
            description = "matrix_n.transpose"
    )
    public FiguraMat3 transpose() {
        double temp;
        temp = v12; v12 = v21; v21 = temp;
        temp = v13; v13 = v31; v31 = temp;
        temp = v23; v23 = v32; v32 = temp;
        cachedInverse = null; //transposing doesn't invalidate the determinant
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload,
            description = "matrix_n.transposed"
    )
    public FiguraMat3 transposed() {
        return super.transposed();
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload,
            description = "matrix_n.invert"
    )
    public FiguraMat3 invert() {
        FiguraMat3 capture = copy();
        if (cachedInverse != null) {
            set(cachedInverse);
            cachedDeterminant = 1 / cachedDeterminant;
        } else {

            double sub11 = v22 * v33 - v23 * v32;
            double sub12 = v21 * v33 - v23 * v31;
            double sub13 = v21 * v32 - v22 * v31;
            double sub21 = v12 * v33 - v13 * v32;
            double sub22 = v11 * v33 - v13 * v31;
            double sub23 = v11 * v32 - v12 * v31;
            double sub31 = v12 * v23 - v13 * v22;
            double sub32 = v11 * v23 - v13 * v21;
            double sub33 = v11 * v22 - v12 * v21;

            double det = v11 * sub11 - v12 * sub12 + v13 * sub13;
            if (det == 0) det = Double.MIN_VALUE;
            det = 1/det;
            cachedDeterminant = det;
            set(
                    det * sub11,
                    -det * sub12,
                    det * sub13,
                    -det * sub21,
                    det * sub22,
                    -det * sub23,
                    det * sub31,
                    -det * sub32,
                    det * sub33
            );
        }
        cachedInverse = capture;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload,
            description = "matrix_n.inverted"
    )
    public FiguraMat3 inverted() {
        return super.inverted();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            description = "matrix_n.det"
    )
    public double det() {
        return super.det();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            description = "matrix_n.reset"
    )
    public FiguraMat3 reset() {
        return super.reset();
    }








    // STATIC CREATOR METHODS
    //----------------------------------------------------------------
    public static FiguraMat3 createScaleMatrix(double x, double y, double z) {
        FiguraMat3 result = of();
        result.v11 = x;
        result.v22 = y;
        result.v33 = z;
        return result;
    }

    public static FiguraMat3 createXRotationMatrix(double degrees) {
        degrees = Math.toRadians(degrees);
        double s = Math.sin(degrees);
        double c = Math.cos(degrees);
        FiguraMat3 result = of();
        result.v22 = result.v33 = c;
        result.v23 = -s;
        result.v32 = s;
        return result;
    }

    public static FiguraMat3 createYRotationMatrix(double degrees) {
        degrees = Math.toRadians(degrees);
        double s = Math.sin(degrees);
        double c = Math.cos(degrees);
        FiguraMat3 result = of();
        result.v11 = result.v33 = c;
        result.v13 = s;
        result.v31 = -s;
        return result;
    }

    public static FiguraMat3 createZRotationMatrix(double degrees) {
        degrees = Math.toRadians(degrees);
        double s = Math.sin(degrees);
        double c = Math.cos(degrees);
        FiguraMat3 result = of();
        result.v11 = result.v22 = c;
        result.v12 = -s;
        result.v21 = s;
        return result;
    }

    public static FiguraMat3 createZYXRotationMatrix(double x, double y, double z) {
        x = Math.toRadians(x);
        y = Math.toRadians(y);
        z = Math.toRadians(z);

        double a = Math.cos(x);
        double b = Math.sin(x);
        double c = Math.cos(y);
        double d = Math.sin(y);
        double e = Math.cos(z);
        double f = Math.sin(z);

        FiguraMat3 result = of();
        result.v11 = c*e;
        result.v12 = b*d*e - a*f;
        result.v13 = a*d*e + b*f;
        result.v21 = c*f;
        result.v22 = b*d*f + a*e;
        result.v23 = a*d*f - b*e;
        result.v31 = -d;
        result.v32 = b*c;
        result.v33 = a*c;
        return result;
    }

    public static FiguraMat3 createTranslationMatrix(double x, double y) {
        FiguraMat3 result = of();
        result.v13 = x;
        result.v23 = y;
        return result;
    }

    public void scale(double x, double y, double z) {
        v11 *= x;
        v12 *= x;
        v13 *= x;
        v21 *= y;
        v22 *= y;
        v23 *= y;
        v31 *= z;
        v32 *= z;
        v33 *= z;
        invalidate();
    }

    public void rotateX(double degrees) {
        degrees = Math.toRadians(degrees);
        double c = Math.cos(degrees);
        double s = Math.sin(degrees);

        double nv21 = c*v21 - s*v31;
        double nv22 = c*v22 - s*v32;
        double nv23 = c*v23 - s*v33;

        v31 = s*v21 + c*v31;
        v32 = s*v22 + c*v32;
        v33 = s*v23 + c*v33;

        v21 = nv21;
        v22 = nv22;
        v23 = nv23;
        invalidate();
    }

    public void rotateY(double degrees) {
        degrees = Math.toRadians(degrees);
        double c = Math.cos(degrees);
        double s = Math.sin(degrees);

        double nv11 = c*v11 + s*v31;
        double nv12 = c*v12 + s*v32;
        double nv13 = c*v13 + s*v33;

        v31 = c*v31 - s*v11;
        v32 = c*v32 - s*v12;
        v33 = c*v33 - s*v13;

        v11 = nv11;
        v12 = nv12;
        v13 = nv13;
        invalidate();
    }

    public void rotateZ(double degrees) {
        degrees = Math.toRadians(degrees);
        double c = Math.cos(degrees);
        double s = Math.sin(degrees);

        double nv11 = c*v11 - s*v21;
        double nv12 = c*v12 - s*v22;
        double nv13 = c*v13 - s*v23;

        v21 = c*v21 + s*v11;
        v22 = c*v22 + s*v12;
        v23 = c*v23 + s*v13;

        v11 = nv11;
        v12 = nv12;
        v13 = nv13;
        invalidate();
    }

    //Rotates using ZYX matrix order, meaning the X axis, then Y, then Z.
    public void rotateZYX(double x, double y, double z) {
        x = Math.toRadians(x);
        y = Math.toRadians(y);
        z = Math.toRadians(z);

        double a = Math.cos(x);
        double b = Math.sin(x);
        double c = Math.cos(y);
        double d = Math.sin(y);
        double e = Math.cos(z);
        double f = Math.sin(z);

        double bc = b*c;
        double ac = a*c;
        double ce = c*e;
        double cf = c*f;
        double p1 = (b*d*e - a*f);
        double p2 = (a*d*e + b*f);
        double p3 = (a*e + b*d*f);
        double p4 = (a*d*f - b*e);

        double nv11 = ce*v11 + p1*v21 + p2*v31;
        double nv21 = cf*v11 + p3*v21 + p4*v31;
        double nv31 = -d*v11 + bc*v21 + ac*v31;

        double nv12 = ce*v12 + p1*v22 + p2*v32;
        double nv22 = cf*v12 + p3*v22 + p4*v32;
        double nv32 = -d*v12 + bc*v22 + ac*v32;

        double nv13 = ce*v13 + p1*v23 + p2*v33;
        double nv23 = cf*v13 + p3*v23 + p4*v33;
        double nv33 = -d*v13 + bc*v23 + ac*v33;

        v11 = nv11;
        v21 = nv21;
        v31 = nv31;
        v12 = nv12;
        v22 = nv22;
        v32 = nv32;
        v13 = nv13;
        v23 = nv23;
        v33 = nv33;
        invalidate();
    }

    public FiguraMat4 augmented() {
        FiguraMat4 result = FiguraMat4.of();
        result.set(v11, v21, v31, 0, v12, v22, v32, 0, v13, v23, v33, 0, 0, 0, 0, 1);
        return result;
    }
}
