package org.diplom_back.modules.auth.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Entity
@Table(name = "client")
@Data
public class Client {
    @Id
    @Column(name = "client_id", length = 36)
    private String clientId = UUID.randomUUID().toString();

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "bonus_points")
    private Integer bonusPoints = 0;

    @OneToOne
    @JsonBackReference
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private User user;
    // Client.java

}