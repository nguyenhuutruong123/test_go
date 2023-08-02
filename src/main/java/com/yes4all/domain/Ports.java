package com.yes4all.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

/**
 * A user.
 */
@Entity
@Getter
@Setter
@Table(name = "ports")
public class Ports implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "title")
    private String title;

    @Column(name = "can_import")
    private Integer canImport;

    @Column(name = "can_export")
    private Integer canExport;

    @Column(name = "country")
    private String country;

    @Column(name = "published")
    private Integer published;
}
