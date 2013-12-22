CREATE TABLE IF NOT EXISTS makers(
  id int(11) unsigned NOT NULL AUTO_INCREMENT,
  name varchar(255) NOT NULL DEFAULT '',
  site varchar(255) NOT NULL DEFAULT '',
  picture varchar(255) NOT NULL DEFAULT '',
  categories int(1) unsigned NOT NULL DEFAULT '0',
  status int(2) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (id),
  KEY cs(categories,status)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=0;