package com.farukgenc.boilerplate.springboot.repository;

import com.farukgenc.boilerplate.springboot.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void whenFindByUsername_thenReturnUser() {
        // given
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("testuser@example.com");
        user.setPassword("password");
        entityManager.persist(user);
        entityManager.flush();

        // when
        User found = userRepository.findByUsername(user.getUsername());

        // then
        assertThat(found).isNotNull();
        assertThat(found.getUsername()).isEqualTo(user.getUsername());
    }

    @Test
    public void whenFindByUsername_withNonExistingUsername_thenReturnNull() {
        // when
        User found = userRepository.findByUsername("nonexistinguser");

        // then
        assertThat(found).isNull();
    }

    @Test
    public void whenExistsByEmail_thenReturnTrue() {
        // given
        User user = new User();
        user.setUsername("testuser2");
        user.setEmail("testuser2@example.com");
        user.setPassword("password");
        entityManager.persist(user);
        entityManager.flush();

        // when
        boolean exists = userRepository.existsByEmail(user.getEmail());

        // then
        assertThat(exists).isTrue();
    }

    @Test
    public void whenExistsByEmail_withNonExistingEmail_thenReturnFalse() {
        // when
        boolean exists = userRepository.existsByEmail("nonexistingemail@example.com");

        // then
        assertThat(exists).isFalse();
    }

    @Test
    public void whenExistsByUsername_thenReturnTrue() {
        // given
        User user = new User();
        user.setUsername("testuser3");
        user.setEmail("testuser3@example.com");
        user.setPassword("password");
        entityManager.persist(user);
        entityManager.flush();

        // when
        boolean exists = userRepository.existsByUsername(user.getUsername());

        // then
        assertThat(exists).isTrue();
    }

    @Test
    public void whenExistsByUsername_withNonExistingUsername_thenReturnFalse() {
        // when
        boolean exists = userRepository.existsByUsername("nonexistingusername");

        // then
        assertThat(exists).isFalse();
    }

    @Test
    public void whenSaveAndFindById_thenReturnUser() {
        // given
        User user = new User();
        user.setUsername("testuser4");
        user.setEmail("testuser4@example.com");
        user.setPassword("password");

        // when
        user = userRepository.save(user);
        Optional<User> found = userRepository.findById(user.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo(user.getUsername());
        assertThat(found.get().getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    public void whenFindAll_thenReturnUserList() {
        // given
        User user1 = new User();
        user1.setUsername("testuser5");
        user1.setEmail("testuser5@example.com");
        user1.setPassword("password");
        entityManager.persist(user1);

        User user2 = new User();
        user2.setUsername("testuser6");
        user2.setEmail("testuser6@example.com");
        user2.setPassword("password");
        entityManager.persist(user2);
        entityManager.flush();

        // when
        List<User> users = userRepository.findAll();

        // then
        assertThat(users).isNotEmpty();
        assertThat(users.size()).isEqualTo(2); // Or more, depending on other tests and test data setup
    }

    @Test
    public void whenDeleteById_thenUserShouldBeDeleted() {
        // given
        User user = new User();
        user.setUsername("testuser7");
        user.setEmail("testuser7@example.com");
        user.setPassword("password");
        user = entityManager.persistAndFlush(user); // Ensure ID is generated and user is persisted

        // when
        userRepository.deleteById(user.getId());
        Optional<User> found = userRepository.findById(user.getId());

        // then
        assertThat(found).isNotPresent();
    }
}
