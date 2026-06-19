package vallegrande.edu.pe.visons.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vallegrande.edu.pe.visons.model.UserAccount;

@Repository
public interface UserRoleRepository extends JpaRepository<UserAccount, Integer> {

    @Query(value = "SELECT role_id FROM USER_ROLES WHERE user_id = :userId", nativeQuery = true)
    List<Integer> findRoleIdsByUserId(@Param("userId") Integer userId);

    @Query(value = "SELECT user_id FROM USER_ROLES WHERE role_id = :roleId", nativeQuery = true)
    List<Integer> findUserIdsByRoleId(@Param("roleId") Integer roleId);

    @Query(value = "SELECT COUNT(*) FROM USER_ROLES WHERE role_id = :roleId", nativeQuery = true)
    Long countUsersByRoleId(@Param("roleId") Integer roleId);

    @Modifying
    @Query(value = """
            INSERT INTO USER_ROLES (user_id, role_id)
            SELECT :userId, :roleId
            WHERE NOT EXISTS (
                SELECT 1 FROM USER_ROLES WHERE user_id = :userId AND role_id = :roleId
            )
            """, nativeQuery = true)
    int assignRole(@Param("userId") Integer userId, @Param("roleId") Integer roleId);

    @Modifying
    @Query(value = "DELETE FROM USER_ROLES WHERE role_id = :roleId", nativeQuery = true)
    int removeAssignmentsByRoleId(@Param("roleId") Integer roleId);
}