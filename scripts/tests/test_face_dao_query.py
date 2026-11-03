"""Behavioral regression tests for the real Room SQL used by photo face chips."""

import pathlib
import re
import sqlite3
import unittest

ROOT = pathlib.Path(__file__).resolve().parents[2]
DAO_SOURCE = ROOT / "app/src/main/java/com/inferno/gallery/data/db/FaceDao.kt"


def photo_people_query():
    kotlin = DAO_SOURCE.read_text(encoding="utf-8")
    match = re.search(
        r'@Query\("""\s*(SELECT\s+f\.id AS faceId,.*?)\s*"""\)\s*'
        r'fun observePeopleForMedia\(mediaId: Long\)',
        kotlin,
        flags=re.DOTALL,
    )
    if not match:
        raise AssertionError("Cannot locate Room SQL for observePeopleForMedia")
    return match.group(1)


class FacePeopleQueryTest(unittest.TestCase):
    def setUp(self):
        self.db = sqlite3.connect(":memory:")
        self.db.row_factory = sqlite3.Row
        self.db.executescript(
            """
            CREATE TABLE persons (
                id INTEGER PRIMARY KEY, name TEXT, coverCropPath TEXT, isHidden INTEGER
            );
            CREATE TABLE faces (
                id INTEGER PRIMARY KEY, mediaId INTEGER, personId INTEGER, cropCachePath TEXT
            );
            CREATE TABLE core_media (
                id INTEGER PRIMARY KEY, bucketName TEXT, uriString TEXT, filePath TEXT
            );
            CREATE TABLE vault_media (originalUri TEXT, originalPath TEXT);
            INSERT INTO persons VALUES (10,'A',NULL,0),(11,'B',NULL,0);
            INSERT INTO core_media VALUES (1,'Camera','media://1','/camera/a.jpg');
            INSERT INTO faces VALUES (101,1,10,'/cache/101.webp'),
                                    (102,1,10,'/cache/102.webp'),
                                    (103,1,11,'/cache/103.webp');
            """
        )
        self.query = photo_people_query()

    def tearDown(self):
        self.db.close()

    def rows(self):
        return self.db.execute(self.query, {"mediaId": 1}).fetchall()

    def test_two_faces_assigned_to_same_person_both_stay_visible(self):
        ids = [row["faceId"] for row in self.rows()]
        self.assertEqual([101, 102, 103], sorted(ids))

    def test_hidden_person_faces_are_still_filtered(self):
        self.db.execute("UPDATE persons SET isHidden=1 WHERE id=11")
        self.assertEqual([101, 102], sorted(row["faceId"] for row in self.rows()))

    def test_unassigned_face_remains_visible(self):
        self.db.execute("INSERT INTO faces VALUES (104,1,NULL,'/cache/104.webp')")
        self.assertEqual([101, 102, 103, 104], sorted(row["faceId"] for row in self.rows()))

    def test_other_photo_does_not_leak_into_results(self):
        self.db.execute("INSERT INTO faces VALUES (105,2,10,'/cache/105.webp')")
        self.assertEqual([101, 102, 103], sorted(row["faceId"] for row in self.rows()))


if __name__ == "__main__":
    unittest.main()
