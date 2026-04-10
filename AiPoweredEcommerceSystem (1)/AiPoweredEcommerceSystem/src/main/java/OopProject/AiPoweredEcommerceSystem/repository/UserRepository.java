package OopProject.AiPoweredEcommerceSystem.repository;


import OopProject.AiPoweredEcommerceSystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Spring Data repository for {@link User} entities.
 *
 * <p>Inherits standard CRUD + paging operations from JpaRepository.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /** Find a user by their unique email address (used during login). */
    Optional<User> findByEmail(String email);

    /** Check if an email is already registered. */
    boolean existsByEmail(String email);
}
