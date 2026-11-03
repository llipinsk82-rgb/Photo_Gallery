"""Regression tests for counts of public photos in Room face-person queries.

A protected image may get a new MediaStore URI while retaining its original
path. Every person count must exclude it using BOTH vault identifiers.
"""

from pathlib import Path
import re
import sqlite3
import unittest

SOURCE = Path(__file__).resolve().parents[2] / "app/src/main/java/com/inferno/gallery/data/db/FaceDao.kt"


def room_query(name):
    text = SOURCE.read_text(encoding="utf-8")
    pattern = (r'@Query\("""\s*(SELECT(?:(?!""").)*?)\s*"""\)\s*'
               r'(?:suspend\s+)?fun\s+' + re.escape(name) + r'\(')
    match = re.search(pattern, text, flags=re.DOTALL)
    if not match:
        raise AssertionError(f"Room SQL for {name} was not found")
    return match.group(1)


class FacePersonVaultPrivacyTest(unittest.TestCase):
    def setUp(self):
        self.db = sqlite3.connect(":memory:")
        self.db.row_factory = sqlite3.Row
        self.db.executescript(
            """
            CREATE TABLE persons (
                id INTEGER PRIMARY KEY, name TEXT, coverCropPath TEXT,
                centroid BLOB, isPromoted INTEGER, isHidden INTEGER,
                isFavorite INTEGER, createdAt INTEGER
            );
            CREATE TABLE faces (id INTEGER PRIMARY KEY, mediaId INTEGER, personId INTEGER);
            CREATE TABLE core_media (
                id INTEGER PRIMARY KEY, bucketName TEXT, uriString TEXT, filePath TEXT
            );
            CREATE TABLE vault_media (originalUri TEXT NOT NULL, originalPath TEXT NOT NULL);

            INSERT INTO persons VALUES
                (10,'Julia',NULL,NULL,1,0,0,1),
                (11,'Vanessa',NULL,NULL,1,1,0,1),
                (12,'Orphan',NULL,NULL,1,0,0,1);
            INSERT INTO core_media VALUES
                (1,'Camera','content://new-julia','/private/julia.jpg'),
                (2,'Camera','content://public-julia','/public/julia.jpg'),
                (3,'Camera','content://new-vanessa','/private/vanessa.jpg'),
                (4,'Camera','content://public-vanessa','/public/vanessa.jpg'),
                (5,'Trash','content://trash-julia','/trash/julia.jpg'),
                (6,'Trash','content://trash-vanessa','/trash/vanessa.jpg'),
                (7,'Camera','content://new-orphan','/private/orphan.jpg');
            INSERT INTO faces VALUES
                (101,1,10),(102,2,10),(103,3,11),(104,4,11),
                (105,5,10),(106,6,11),(107,7,12);
            INSERT INTO vault_media VALUES
                ('content://old-julia','/private/julia.jpg'),
                ('content://old-vanessa','/private/vanessa.jpg'),
                ('content://old-orphan','/private/orphan.jpg');
            """
        )

    def tearDown(self):
        self.db.close()

    def test_promoted_person_count_ignores_vault_photo_by_path(self):
        rows = self.db.execute(room_query("observePromotedPersons")).fetchall()
        self.assertEqual([(10, 1)], [(r["id"], r["faceCount"]) for r in rows])

    def test_visible_person_count_ignores_vault_photo_by_path(self):
        rows = self.db.execute(room_query("observeAllVisiblePersons")).fetchall()
        self.assertEqual([(10, 1)], [(r["id"], r["faceCount"]) for r in rows])

    def test_hidden_person_count_ignores_vault_photo_by_path(self):
        rows = self.db.execute(room_query("observeHiddenPersons")).fetchall()
        self.assertEqual([(11, 1)], [(r["id"], r["faceCount"]) for r in rows])

    def test_hide_show_sheet_count_ignores_vault_photo_by_path(self):
        rows = self.db.execute(room_query("observeAllPersonsIncludingHidden")).fetchall()
        self.assertEqual([(10, 1), (11, 1), (12, 0)],
                         sorted((r["id"], r["faceCount"]) for r in rows))

    def test_vault_only_person_has_no_public_photos(self):
        rows = self.db.execute(room_query("observeAllPersonsIncludingHidden")).fetchall()
        self.assertEqual(0, next(r["faceCount"] for r in rows if r["id"] == 12))
        self.assertNotIn(12, [r["id"] for r in
                             self.db.execute(room_query("observeAllVisiblePersons")).fetchall()])

    def test_name_search_count_ignores_vault_photo_by_path(self):
        rows = self.db.execute(room_query("searchPersonsByName"), {"query": "Jul"}).fetchall()
        self.assertEqual([(10, 1)], [(r["id"], r["faceCount"]) for r in rows])


if __name__ == "__main__":
    unittest.main()
