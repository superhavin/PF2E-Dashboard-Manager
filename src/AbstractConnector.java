interface AbstractConnector {

    /**
     * Turns a Json Array to an array of Objects.
     * @param jsonArrayAttribute a JSON_ARRAY from an attribute.
     * @return an array from JSON resultSet of statement.
     */
    public Object[] toJsonArray(final String jsonArrayAttribute); //Takes JSON_ARRAY{"Common", "Elven"}

    /**
     * Turns a Json Object to an 2D array of Objects.
     * @param jsonObjectAttribute a JSON_OBJECT from an attribute.
     * @return an 2D array from JSON resultSet of statement.
     */
    public Object[][] toJsonObject(final String jsonObjectAttribute); //Takes JSON_OBJECT{"Table", "ClassFeat"}

    /**
     * Class for the reference_title JSON attributes.
     */
    abstract class Reference{
        final Object tableName;
        final Object[] referenceTitle;

        Reference(final Object tableName, final Object referenceTitle){
            this.tableName = tableName;
            this.referenceTitle = new Object[]{referenceTitle};
        }

        Reference(final Object tableName, final Object... referenceTitle){
            this.tableName = tableName;
            this.referenceTitle = referenceTitle;
        }

        /**
         * Return the first element in the array
         * @return A SELECT statement from the Table with the specific reference
         */
        abstract String returnReferenceToTable();

        /**
         * Sets an array from the Table
         * @param setTableToReference Sets the array to the #referenceTitel
         */
        abstract void setReferencesToTable(Object[] setTableToReference);
    }
}

