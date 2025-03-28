package types;

/**
 * Represents a data type.
 */
public class DataType {
    /** The scalar type - void, int, object */
    private BaseType baseType;
    /** The class name if this is an object */
    private String className;
    /** Number of dimensions */
    private int numDims;

    public DataType(String typeName) {
        switch (typeName) {
        case "int" -> this.baseType = BaseType.INT;
        case "void" -> this.baseType = BaseType.VOID;
        default -> {
            this.baseType = BaseType.OBJECT;
            this.className = typeName;
        }
        }
    }

    public DataType(String typeName, int numDims) {
        this(typeName);
        this.numDims = numDims;
    }

    /**
     * Copies a DataType.
     *
     * @param that the DataType to copy
     */
    public DataType(DataType that) {
        baseType = that.baseType;
        className = that.className;
        numDims = that.numDims;
    }

    public String getClassName() {
        return className;
    }

    public int getNumDims() {
        return numDims;
    }

    /**
     * Gets the type of the elements of this array type.
     *
     * @return the element type
     */
    public DataType getElementType() {
        if (numDims == 0) {
            throw new RuntimeException("Not an array type");
        }

        DataType elemType = new DataType(this);
        elemType.numDims--;
        return elemType;
    }

    /**
     * Returns true if this is an object type (arrays are objects).
     * 
     * @return true if this is an object type
     */
    public boolean isObject() {
        return baseType == BaseType.OBJECT || numDims > 0;
    }

    @Override
    public boolean equals(Object obj) {
        return this.toString().equals(obj.toString());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(baseType == BaseType.OBJECT ? className : baseType);
        for (int i = 0; i < numDims; i++) {
            sb.append("[]");
        }
        return sb.toString();
    }

    public static DataType INT = new DataType("int");
    public static DataType VOID = new DataType("void");

    public enum BaseType {
        VOID,
        INT,
        OBJECT;

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }

        public static BaseType fromString(String str) {
            return BaseType.valueOf(str.toUpperCase());
        }
    }
}
