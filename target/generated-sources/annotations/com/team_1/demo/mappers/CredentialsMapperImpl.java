package com.team_1.demo.mappers;

import com.team_1.demo.dtos.CredentialsDto;
import com.team_1.demo.entities.Credentials;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2023-09-01T18:29:58-0700",
    comments = "version: 1.5.2.Final, compiler: javac, environment: Java 20 (Oracle Corporation)"
)
@Component
public class CredentialsMapperImpl implements CredentialsMapper {

    @Override
    public Credentials credentialsDtoToEntity(CredentialsDto credentialsDto) {
        if ( credentialsDto == null ) {
            return null;
        }

        Credentials credentials = new Credentials();

        credentials.setUsername( credentialsDto.getUsername() );
        credentials.setPassword( credentialsDto.getPassword() );

        return credentials;
    }

    @Override
    public Credentials requestDtoToEntity(CredentialsDto credentialsDto) {
        if ( credentialsDto == null ) {
            return null;
        }

        Credentials credentials = new Credentials();

        credentials.setUsername( credentialsDto.getUsername() );
        credentials.setPassword( credentialsDto.getPassword() );

        return credentials;
    }
}
