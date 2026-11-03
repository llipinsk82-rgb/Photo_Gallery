"""Contract: the public face-scan entrypoint must lock per media before reading faces."""

from pathlib import Path
import re
import unittest

SOURCE = (Path(__file__).resolve().parents[2]
          / "app/src/main/java/com/inferno/gallery/data/ai/FaceScanHelper.kt")


class FaceScanEntryPointTest(unittest.TestCase):
    def test_single_media_scan_is_coordinated_before_work_begins(self):
        source = SOURCE.read_text(encoding="utf-8")
        entry = re.search(
            r'suspend\s+fun\s+scanSingleMedia\s*\([\s\S]*?\)\s*:\s*Int\s*=\s*'
            r'FaceScanCoordinator\.withMediaLock\(mediaId\)',
            source,
        )
        self.assertIsNotNone(entry, "scanSingleMedia must acquire per-media lock before reading DB")


if __name__ == "__main__":
    unittest.main()
