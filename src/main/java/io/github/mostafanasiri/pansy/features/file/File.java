package io.github.mostafanasiri.pansy.features.file;

import io.github.mostafanasiri.pansy.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "files")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class File extends BaseEntity {
    @Column(name = "name", nullable = false)
    private String name;

    public File(String name) {
        this.name = name;
    }
}
