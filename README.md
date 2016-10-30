
# gmjonker/util

Various utilities.

### Git hooks

Install the following pre-commit git hook to do a Maven build before committing. It checks that the code builds correctly
and that the style is ok:

```sh
cd .git/hooks
ln -s ../../utils/git-hooks/pre-commit.hook pre-commit
cd -
```