package Mofit.com.security;
import Mofit.com.security.Token;

import org.springframework.data.repository.CrudRepository;

public interface TokenRepository extends CrudRepository<Token, Long> {
}