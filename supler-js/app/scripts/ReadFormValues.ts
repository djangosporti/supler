class ReadFormValues {
    /**
     * @param element Element from which to read field values
     * @param result The object to which found mappings will be added.
     * @returns An object containing mappings from found form field names to form field values.
     */
    static getValueFrom(element, result = {}) {
        var fieldType = element.getAttribute(SuplerAttributes.FIELD_TYPE);
        var multiple = element.getAttribute(SuplerAttributes.MULTIPLE) === "true";
        if (fieldType) {
            var fieldName = element.getAttribute("name");
            switch (fieldType) {
                case FieldTypes.STRING:
                    ReadFormValues.appendFieldValue(result, fieldName, this.getElementValue(element), multiple);
                    break;

                case FieldTypes.INTEGER:
                    ReadFormValues.appendFieldValue(result, fieldName, this.parseIntOrNull(this.getElementValue(element)), multiple);
                    break;

                case FieldTypes.SELECT:
                    ReadFormValues.appendFieldValue(result, fieldName, this.parseIntOrNull(this.getElementValue(element)), multiple);
                    break;

                case FieldTypes.BOOLEAN:
                    ReadFormValues.appendFieldValue(result, fieldName, this.parseBooleanOrNull(this.getElementValue(element)), multiple);
                    break;

                case FieldTypes.SUBFORM:
                    fieldName = element.getAttribute(SuplerAttributes.FIELD_NAME);
                    var subResult = this.getValueFromChildren(element, {});
                    ReadFormValues.appendFieldValue(result, fieldName, subResult, multiple);
                    break;
            }
        } else if (element.children.length > 0) {
            // flattening
            this.getValueFromChildren(element, result);
        }

        return result;
    }

    private static getValueFromChildren(element, result) {
        var children = element.children;

        for (var i=0; i<children.length; i++) {
            this.getValueFrom(children[i], result);
        }

        return result;
    }

    private static getElementValue(element) {
        if ((element.type === 'radio' || element.type === 'checkbox') && !element.checked) {
            return null;
        } else {
            return element.value;
        }
    }

    private static appendFieldValue(result, fieldName, fieldValue, multiple) {
        if (fieldValue !== null) {
            if (result[fieldName] && multiple) {
                result[fieldName].push(fieldValue);
            } else {
                if (multiple) {
                    result[fieldName] = [ fieldValue ];
                } else {
                    result[fieldName] = fieldValue;
                }
            }
        }
    }

    private static parseIntOrNull(v): number {
        var p = parseInt(v);
        if (isNaN(p)) {
            return null;
        } else {
            return p;
        }
    }

    private static parseBooleanOrNull(v): number {
        var p = parseInt(v);
        if (isNaN(p)) {
            return null;
        } else {
            return p === 1;
        }
    }
}
