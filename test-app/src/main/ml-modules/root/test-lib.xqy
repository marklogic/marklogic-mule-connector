xquery version "1.0-ml";

module namespace test = "test";

(:
Prepares the test database by deleting everything that wasn't loaded via the test app. Ensures that each test starts
with a clean and expected database.

Expected to have an amp applied to it so that temporal documents can be deleted. That also depends on the
temporal collection having the "updates-admin-override" option enabled.
:)
declare function prepare-database() {
  for $uri in cts:uris((), (), cts:not-query(cts:collection-query("test-data")))
  return xdmp:document-delete($uri)
};
