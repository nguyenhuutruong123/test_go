package com.yes4all.domain;



import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.Instant;

/**
 * A Photo.
 */
@Getter
@Setter
@Entity
@Table(name = "log_service")
public class LogService implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "function_description")
    private String functionDescription;

    @Column(name = "function_code")
    private String functionCode;

    @Column(name = "function_id")
    private String functionId;

    @Column(name = "payload")
    private String payload;

    @Column(name = "time")
    private Instant time;

    @Column(name = "project")
    private String project;

    @Column(name = "action")
    private String action;

    public Integer getId() {
        return this.id;
    }

    public LogService id(Integer id) {
        this.setId(id);
        return this;
    }

// jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LogService)) {
            return false;
        }
        return id != null && id.equals(((LogService) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }


}
