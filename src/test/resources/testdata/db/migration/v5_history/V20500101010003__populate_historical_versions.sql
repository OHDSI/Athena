-- Each import should be done in separate session, since it use temporary table
SELECT public.import_new_version('public', 'test');
SELECT public.import_new_version('public', 'test_1');
SELECT public.import_new_version('public', 'test_2');
