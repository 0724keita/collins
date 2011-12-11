# -- Initial schema

# --- !Ups

CREATE TABLE status (
  id                            INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name                          VARCHAR(16) NOT NULL UNIQUE,
  description                   VARCHAR(255) NOT NULL
);
CREATE TABLE asset_type (
  id                            INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name                          VARCHAR(255) NOT NULL UNIQUE
);
CREATE TABLE asset (
  id                            BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  secondary_id                  VARCHAR(255) NOT NULL UNIQUE,
  status                        INTEGER NOT NULL,
  asset_type                    INTEGER NOT NULL,
  created                       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated                       TIMESTAMP,
  deleted                       TIMESTAMP,
  CONSTRAINT fk_status FOREIGN KEY (status) REFERENCES status(id),
  CONSTRAINT fk_type FOREIGN KEY (asset_type) REFERENCES asset_type (id)
);
CREATE INDEX status_idx ON asset (status);
CREATE INDEX asset_type_idx ON asset (asset_type);
CREATE INDEX created_idx ON asset (created);

CREATE TABLE asset_meta (
  id                            INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name                          VARCHAR(255) NOT NULL UNIQUE,
  priority                      INTEGER NOT NULL DEFAULT -1,
  label                         VARCHAR(255) NOT NULL,
  description                   VARCHAR(255) NOT NULL
);
CREATE INDEX asset_meta_idx ON asset_meta (priority);

CREATE TABLE asset_meta_value (
  asset_id                      BIGINT NOT NULL,
  asset_meta_id                 INTEGER NOT NULL,
  value                         TEXT,
  CONSTRAINT fk_amv_asset_id      FOREIGN KEY (asset_id) REFERENCES asset (id) ON DELETE CASCADE,
  CONSTRAINT fk_amv_asset_meta_id FOREIGN KEY (asset_meta_id) REFERENCES asset_meta (id)
);
CREATE INDEX amv_ids ON asset_meta_value (asset_id, asset_meta_id);
CREATE INDEX amv_mid ON asset_meta_value (asset_meta_id);

# --- !Downs

DROP TABLE IF EXISTS asset_meta_value;
DROP TABLE IF EXISTS asset_meta;
DROP TABLE IF EXISTS asset;
DROP TABLE IF EXISTS asset_type;
DROP TABLE IF EXISTS status;
