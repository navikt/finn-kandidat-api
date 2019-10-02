/**
 * Autogenerated by Avro
 *
 * DO NOT EDIT DIRECTLY
 */
package no.nav.tag.finnkandidatapi.kafka;

import org.apache.avro.specific.SpecificData;

@SuppressWarnings("all")
@org.apache.avro.specific.AvroGenerated
public class InkluderingsKandidat extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  private static final long serialVersionUID = 1486218340691393885L;
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"InkluderingsKandidat\",\"namespace\":\"no.nav.tag.finnkandidatapi.kafka\",\"fields\":[{\"name\":\"aktørId\",\"type\":\"string\"},{\"name\":\"harTilretteleggingsbehov\",\"type\":\"boolean\"}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }
  @Deprecated public java.lang.CharSequence aktørId;
  @Deprecated public boolean harTilretteleggingsbehov;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use <code>newBuilder()</code>.
   */
  public InkluderingsKandidat() {}

  /**
   * All-args constructor.
   * @param aktørId The new value for aktørId
   * @param harTilretteleggingsbehov The new value for harTilretteleggingsbehov
   */
  public InkluderingsKandidat(java.lang.CharSequence aktørId, java.lang.Boolean harTilretteleggingsbehov) {
    this.aktørId = aktørId;
    this.harTilretteleggingsbehov = harTilretteleggingsbehov;
  }

  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call.
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return aktørId;
    case 1: return harTilretteleggingsbehov;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  // Used by DatumReader.  Applications should not call.
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: aktørId = (java.lang.CharSequence)value$; break;
    case 1: harTilretteleggingsbehov = (java.lang.Boolean)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  /**
   * Gets the value of the 'aktørId' field.
   * @return The value of the 'aktørId' field.
   */
  public java.lang.CharSequence getAktørId() {
    return aktørId;
  }

  /**
   * Sets the value of the 'aktørId' field.
   * @param value the value to set.
   */
  public void setAktørId(java.lang.CharSequence value) {
    this.aktørId = value;
  }

  /**
   * Gets the value of the 'harTilretteleggingsbehov' field.
   * @return The value of the 'harTilretteleggingsbehov' field.
   */
  public java.lang.Boolean getHarTilretteleggingsbehov() {
    return harTilretteleggingsbehov;
  }

  /**
   * Sets the value of the 'harTilretteleggingsbehov' field.
   * @param value the value to set.
   */
  public void setHarTilretteleggingsbehov(java.lang.Boolean value) {
    this.harTilretteleggingsbehov = value;
  }

  /**
   * Creates a new InkluderingsKandidat RecordBuilder.
   * @return A new InkluderingsKandidat RecordBuilder
   */
  public static no.nav.tag.finnkandidatapi.kafka.InkluderingsKandidat.Builder newBuilder() {
    return new no.nav.tag.finnkandidatapi.kafka.InkluderingsKandidat.Builder();
  }

  /**
   * Creates a new InkluderingsKandidat RecordBuilder by copying an existing Builder.
   * @param other The existing builder to copy.
   * @return A new InkluderingsKandidat RecordBuilder
   */
  public static no.nav.tag.finnkandidatapi.kafka.InkluderingsKandidat.Builder newBuilder(no.nav.tag.finnkandidatapi.kafka.InkluderingsKandidat.Builder other) {
    return new no.nav.tag.finnkandidatapi.kafka.InkluderingsKandidat.Builder(other);
  }

  /**
   * Creates a new InkluderingsKandidat RecordBuilder by copying an existing InkluderingsKandidat instance.
   * @param other The existing instance to copy.
   * @return A new InkluderingsKandidat RecordBuilder
   */
  public static no.nav.tag.finnkandidatapi.kafka.InkluderingsKandidat.Builder newBuilder(no.nav.tag.finnkandidatapi.kafka.InkluderingsKandidat other) {
    return new no.nav.tag.finnkandidatapi.kafka.InkluderingsKandidat.Builder(other);
  }

  /**
   * RecordBuilder for InkluderingsKandidat instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<InkluderingsKandidat>
    implements org.apache.avro.data.RecordBuilder<InkluderingsKandidat> {

    private java.lang.CharSequence aktørId;
    private boolean harTilretteleggingsbehov;

    /** Creates a new Builder */
    private Builder() {
      super(SCHEMA$);
    }

    /**
     * Creates a Builder by copying an existing Builder.
     * @param other The existing Builder to copy.
     */
    private Builder(no.nav.tag.finnkandidatapi.kafka.InkluderingsKandidat.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.aktørId)) {
        this.aktørId = data().deepCopy(fields()[0].schema(), other.aktørId);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.harTilretteleggingsbehov)) {
        this.harTilretteleggingsbehov = data().deepCopy(fields()[1].schema(), other.harTilretteleggingsbehov);
        fieldSetFlags()[1] = true;
      }
    }

    /**
     * Creates a Builder by copying an existing InkluderingsKandidat instance
     * @param other The existing instance to copy.
     */
    private Builder(no.nav.tag.finnkandidatapi.kafka.InkluderingsKandidat other) {
            super(SCHEMA$);
      if (isValidValue(fields()[0], other.aktørId)) {
        this.aktørId = data().deepCopy(fields()[0].schema(), other.aktørId);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.harTilretteleggingsbehov)) {
        this.harTilretteleggingsbehov = data().deepCopy(fields()[1].schema(), other.harTilretteleggingsbehov);
        fieldSetFlags()[1] = true;
      }
    }

    /**
      * Gets the value of the 'aktørId' field.
      * @return The value.
      */
    public java.lang.CharSequence getAktørId() {
      return aktørId;
    }

    /**
      * Sets the value of the 'aktørId' field.
      * @param value The value of 'aktørId'.
      * @return This builder.
      */
    public no.nav.tag.finnkandidatapi.kafka.InkluderingsKandidat.Builder setAktørId(java.lang.CharSequence value) {
      validate(fields()[0], value);
      this.aktørId = value;
      fieldSetFlags()[0] = true;
      return this;
    }

    /**
      * Checks whether the 'aktørId' field has been set.
      * @return True if the 'aktørId' field has been set, false otherwise.
      */
    public boolean hasAktørId() {
      return fieldSetFlags()[0];
    }


    /**
      * Clears the value of the 'aktørId' field.
      * @return This builder.
      */
    public no.nav.tag.finnkandidatapi.kafka.InkluderingsKandidat.Builder clearAktørId() {
      aktørId = null;
      fieldSetFlags()[0] = false;
      return this;
    }

    /**
      * Gets the value of the 'harTilretteleggingsbehov' field.
      * @return The value.
      */
    public java.lang.Boolean getHarTilretteleggingsbehov() {
      return harTilretteleggingsbehov;
    }

    /**
      * Sets the value of the 'harTilretteleggingsbehov' field.
      * @param value The value of 'harTilretteleggingsbehov'.
      * @return This builder.
      */
    public no.nav.tag.finnkandidatapi.kafka.InkluderingsKandidat.Builder setHarTilretteleggingsbehov(boolean value) {
      validate(fields()[1], value);
      this.harTilretteleggingsbehov = value;
      fieldSetFlags()[1] = true;
      return this;
    }

    /**
      * Checks whether the 'harTilretteleggingsbehov' field has been set.
      * @return True if the 'harTilretteleggingsbehov' field has been set, false otherwise.
      */
    public boolean hasHarTilretteleggingsbehov() {
      return fieldSetFlags()[1];
    }


    /**
      * Clears the value of the 'harTilretteleggingsbehov' field.
      * @return This builder.
      */
    public no.nav.tag.finnkandidatapi.kafka.InkluderingsKandidat.Builder clearHarTilretteleggingsbehov() {
      fieldSetFlags()[1] = false;
      return this;
    }

    @Override
    public InkluderingsKandidat build() {
      try {
        InkluderingsKandidat record = new InkluderingsKandidat();
        record.aktørId = fieldSetFlags()[0] ? this.aktørId : (java.lang.CharSequence) defaultValue(fields()[0]);
        record.harTilretteleggingsbehov = fieldSetFlags()[1] ? this.harTilretteleggingsbehov : (java.lang.Boolean) defaultValue(fields()[1]);
        return record;
      } catch (Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }

  private static final org.apache.avro.io.DatumWriter
    WRITER$ = new org.apache.avro.specific.SpecificDatumWriter(SCHEMA$);

  @Override public void writeExternal(java.io.ObjectOutput out)
    throws java.io.IOException {
    WRITER$.write(this, SpecificData.getEncoder(out));
  }

  private static final org.apache.avro.io.DatumReader
    READER$ = new org.apache.avro.specific.SpecificDatumReader(SCHEMA$);

  @Override public void readExternal(java.io.ObjectInput in)
    throws java.io.IOException {
    READER$.read(this, SpecificData.getDecoder(in));
  }

}