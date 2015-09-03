package kth.exjobb.autodermo;

import android.content.Context;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class that builds a form from a json-structured
 * string. This modifies the passed view by adding widgets
 * directly onto it.
 */

public class FormBuilder {
    private static final String ELEMENT_TYPE_NUMBER = "number";
    private static final String ELEMENT_TYPE_OPTION = "option";

    public static List<ElementGroup> build(LinearLayout layout, String json) throws IllegalArgumentException {
        List<ElementGroup> elements = new ArrayList<ElementGroup>();

        try {
            JSONArray formStructure = new JSONArray(json);

            for(int i = 0, l = formStructure.length();i < l;i++){
                JSONObject element = formStructure.getJSONObject(i);

                if(element.has("type")){
                    String type = element.getString("type");

                    ElementGroup widget = null;

                    // there are better ways to do this but since we
                    // only have two input types, we simply use an if
                    if(type.equals(ELEMENT_TYPE_NUMBER)){
                        widget = new NumberElement(layout.getContext(), element);
                    } else if(type.equals(ELEMENT_TYPE_OPTION)){
                        widget = new OptionElement(layout.getContext(), element);
                    }

                    if (widget != null) {
                        widget.setIdentifier(element.getString("id"));
                    }

                    if(widget != null) {
                        layout.addView(widget);
                        elements.add(widget);
                    }
                }
            }
        } catch (JSONException e) {
            throw new IllegalArgumentException();
        }

        return elements;
    }

    /**
     * Abstract class that encapsulates an Element (and its respective
     * components such as labels)
     */
    abstract public static class ElementGroup extends LinearLayout {
        protected String identifier;

        public ElementGroup(Context context) {
            super(context);

            setOrientation(LinearLayout.VERTICAL);
        }

        public void setIdentifier(String id){
            identifier = id;
        }

        public abstract JSONObject getValue() throws JSONException;
    }

    /**
     * Builds a Number input widget, and handles its output
     */
    private static class NumberElement extends ElementGroup {
        private EditText numberElement;

        public NumberElement(Context context, JSONObject def) {
            super(context);

            TextView label = new TextView(context);
            try {
                label.setText(def.getString("question"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            numberElement = new EditText(context);
            numberElement.setInputType(InputType.TYPE_CLASS_NUMBER);
            numberElement.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            addView(label);
            addView(numberElement);
        }

        public JSONObject getValue() throws JSONException {
            // we get the value as a string
            // because it is more flexible than using an int
            // in our case where we are not processing it but simply passing
            // it back to the server. It is then the responsibility of the server
            // to parse it as required.
            String value = numberElement.getText().toString();

            if(value.equals("")) value = "0";

            JSONObject obj = new JSONObject();

            obj.put("id", identifier);
            obj.put("value", value);

            return obj;
        }
    }

    /**
     * Builds an option list
     */
    private static class OptionElement extends ElementGroup {
        private Spinner optionElement;

        public OptionElement(Context context, JSONObject def) {
            super(context);

            TextView label = new TextView(context);
            try {
                label.setText(def.getString("question"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            optionElement = new Spinner(context);
            optionElement.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            List<String> list = new ArrayList<String>();
            try {
                JSONArray options = def.getJSONArray("options");

                for(int i = 0, l = options.length();i < l;i++){
                    list.add(options.getString(i));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, list);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            optionElement.setAdapter(adapter);

            addView(label);
            addView(optionElement);
        }

        public JSONObject getValue() throws JSONException {
            // The value represents the index of the selection.
            // Again, it is the responsibility of the server to determine
            // its significance.
            int pos = optionElement.getSelectedItemPosition();
            JSONObject obj = new JSONObject();

            obj.put("id", identifier);
            obj.put("value", pos);

            return obj;
        }
    }
}
