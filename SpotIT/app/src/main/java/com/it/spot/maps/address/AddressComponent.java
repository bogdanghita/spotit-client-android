
package com.it.spot.maps.address;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")
public class AddressComponent {

    @SerializedName("long_name")
    @Expose
    private String longName;
    @SerializedName("short_name")
    @Expose
    private String shortName;
    @SerializedName("types")
    @Expose
    private List<String> types = new ArrayList<String>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public AddressComponent() {
    }

    /**
     * 
     * @param longName
     * @param types
     * @param shortName
     */
    public AddressComponent(String longName, String shortName, List<String> types) {
        this.longName = longName;
        this.shortName = shortName;
        this.types = types;
    }

    /**
     * 
     * @return
     *     The longName
     */
    public String getLongName() {
        return longName;
    }

    /**
     * 
     * @param longName
     *     The long_name
     */
    public void setLongName(String longName) {
        this.longName = longName;
    }

    /**
     * 
     * @return
     *     The shortName
     */
    public String getShortName() {
        return shortName;
    }

    /**
     * 
     * @param shortName
     *     The short_name
     */
    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    /**
     * 
     * @return
     *     The types
     */
    public List<String> getTypes() {
        return types;
    }

    /**
     * 
     * @param types
     *     The types
     */
    public void setTypes(List<String> types) {
        this.types = types;
    }

}
