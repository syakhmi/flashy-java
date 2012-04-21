DROP TABLE IF EXISTS "Cards";
CREATE TABLE "Cards" ("id" INTEGER PRIMARY KEY  NOT NULL , "question" nvarchar NOT NULL , "answer" nvarchar NOT NULL , "listID" INTEGER NOT NULL );
INSERT INTO "Cards" VALUES(1,'El burro','Donkey',17);
INSERT INTO "Cards" VALUES(2,'La vaca','Cow',17);
INSERT INTO "Cards" VALUES(3,'La manzana','Apple',18);
INSERT INTO "Cards" VALUES(4,'La naranja','Orange',18);
INSERT INTO "Cards" VALUES(5,'かば','hippopotamus',1);
INSERT INTO "Cards" VALUES(6,'キリン','giraffe',1);
INSERT INTO "Cards" VALUES(7,'バナナ','banana',2);
INSERT INTO "Cards" VALUES(8,'いちご','strawberry',2);
DROP TABLE IF EXISTS "Categories";
CREATE TABLE Categories (name nvarchar(150), id INTEGER PRIMARY KEY ASC);
INSERT INTO "Categories" VALUES('Japanese',0);
INSERT INTO "Categories" VALUES('Spanish',1);
DROP TABLE IF EXISTS "Lists";
CREATE TABLE "Lists" ("id" INTEGER PRIMARY KEY  NOT NULL , "categoryID" INTEGER NOT NULL , "name" nvarchar(150) NOT NULL );
INSERT INTO "Lists" VALUES(1,0,'Animals');
INSERT INTO "Lists" VALUES(2,0,'Food');
INSERT INTO "Lists" VALUES(17,1,'Animals');
INSERT INTO "Lists" VALUES(18,1,'Food');
CREATE TRIGGER fkdc_Cards_listID_Lists_id
BEFORE DELETE ON Lists
FOR EACH ROW BEGIN
    DELETE FROM Cards WHERE Cards.listID = OLD.id;
END;
CREATE TRIGGER fkdc_Lists_categoryID_Categories_id
BEFORE DELETE ON Categories
FOR EACH ROW BEGIN
    DELETE FROM Lists WHERE Lists.categoryID = OLD.id;
END;
