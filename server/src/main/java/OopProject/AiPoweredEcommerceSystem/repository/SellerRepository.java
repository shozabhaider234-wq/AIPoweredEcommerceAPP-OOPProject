package OopProject.AiPoweredEcommerceSystem.repository;


import OopProject.AiPoweredEcommerceSystem.entity.Seller;
import OopProject.AiPoweredEcommerceSystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SellerRepository extends JpaRepository<Seller, Long> {

    /** Retrieve the seller profile linked to a given user. */
    Optional<Seller> findByUser(User user);

    /** Check whether a user already has a seller profile. */
    boolean existsByUser(User user);
}
