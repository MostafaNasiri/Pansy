package io.github.mostafanasiri.pansy.app.data.entity.jpa;

import io.github.mostafanasiri.pansy.app.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "files")
@EntityListeners(AuditingEntityListener.class)
public class FileEntity extends BaseEntity {
    @Column(name = "name", nullable = false)
    private String name;

    public FileEntity(String name) {
        this.name = name;
    }
}
