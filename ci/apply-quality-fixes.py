#!/usr/bin/env python3
import pathlib
import re
import sys

ROOT = pathlib.Path(
    sys.argv[1] if len(sys.argv) > 1
    else "backend/src/main/java/tn/esprit/backend"
)

if not ROOT.is_dir():
    sys.exit(f"[X] dossier introuvable : {ROOT}")

PATTERN = re.compile(
    r"(\w+) (\w+) = (\w+)\.findById\((\w+)\)\.orElse\(null\);"
)

IMPORT = "import jakarta.persistence.EntityNotFoundException;"
nb_services = 0

for f in sorted((ROOT / "service" / "impl").glob("*.java")):
    src = f.read_text(encoding="utf-8")

    def repl(m):
        type_, var, repo, arg = m.groups()
        return (
            f'{type_} {var} = {repo}.findById({arg})'
            f'.orElseThrow(() -> new EntityNotFoundException('
            f'"{type_} introuvable : " + {arg}));'
        )

    new, n = PATTERN.subn(repl, src)

    if n:
        if IMPORT not in new:
            new = re.sub(
                r"(^import )",
                IMPORT + "\n" + r"\1",
                new,
                count=1,
                flags=re.M
            )

        f.write_text(new, encoding="utf-8")
        nb_services += n
        print(
            f"  [service] {f.name} : "
            f"{n} orElse(null) -> orElseThrow"
        )

nb_cors = 0

for f in sorted((ROOT / "controller").glob("*.java")):
    src = f.read_text(encoding="utf-8")

    new, n = re.subn(
        r'^[ \t]*@CrossOrigin\("\*"\)[ \t]*\n',
        "",
        src,
        flags=re.M
    )

    if n:
        f.write_text(new, encoding="utf-8")
        nb_cors += n
        print(
            f'  [controller] {f.name} : '
            '@CrossOrigin("*") supprime'
        )

print(
    f"==> {nb_services} orElse(null) corriges, "
    f"{nb_cors} @CrossOrigin supprimes"
)
