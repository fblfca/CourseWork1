package courseWork.IRS.service;

import courseWork.IRS.model.Role;
import courseWork.IRS.model.UserInfo;
import courseWork.IRS.repository.RoleRepository;
import courseWork.IRS.repository.UserInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public Role registerUser(String login, String rawPassword, String name, String surname, String phone) {
        if (roleRepository.findByLogin(login).isPresent()) {
            throw new RuntimeException("Пользователь с таким email уже существует");
        }

        Role role = new Role();
        role.setLogin(login);
        role.setPasswordHash(passwordEncoder.encode(rawPassword));
        role.setRole("пользователь");

        UserInfo userInfo = new UserInfo();
        userInfo.setName(name);
        userInfo.setSurname(surname);
        userInfo.setPhone(phone);
        userInfo.setRole(role);

        role.setUserInfo(userInfo);
        return roleRepository.save(role);
    }

    public long getUserCount() {
        return roleRepository.count();
    }

    /**
     * Поиск полной информации о пользователе (UserInfo) по логину (email).
     * @param login Логин пользователя (email).
     * @return Объект UserInfo.
     */
    @Transactional(readOnly = true)
    public UserInfo findByLogin(String login) {
        Optional<Role> role = roleRepository.findByLogin(login);
        return role.map(Role::getUserInfo).orElse(null);
    }
}