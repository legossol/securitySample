package kr.legossol.sampleSecurity.repository;

import java.util.Optional;
import javax.swing.text.html.Option;
import kr.legossol.sampleSecurity.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findOneWithAuthoritiesByUsername(String username);
}
