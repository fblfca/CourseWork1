package courseWork.IRS.repository;

import courseWork.IRS.model.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserInfoRepository extends JpaRepository<UserInfo, Integer> {
    // Метод для поиска пользователя по телефону
    Optional<UserInfo> findByPhone(String phone);
}