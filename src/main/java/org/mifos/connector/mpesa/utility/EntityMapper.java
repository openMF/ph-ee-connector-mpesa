package org.mifos.connector.mpesa.utility;

public interface EntityMapper <ENTITY, DOMAIN> {

    DOMAIN fromEntityToDomain(ENTITY entity);

    ENTITY fromDomainToEntity(DOMAIN domain);

}
