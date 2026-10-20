"""Build this local Java simulator; never writes to the imported robot repository."""
from pathlib import Path
import os, subprocess, shutil, sys

ROOT = Path(__file__).resolve().parent
def jdk():
    candidates = [os.environ.get('JAVA_HOME', ''), str(ROOT/'runtime')]
    candidates += [r'C:\Program Files\Android\Android Studio2\jbr', r'C:\Program Files\Android\Android Studio\jbr']
    for p in candidates:
        if p and (Path(p)/'bin/javac.exe').exists(): return Path(p)
    p = shutil.which('javac')
    if p: return Path(p).resolve().parent.parent
    raise SystemExit('A JDK 21+ is required. Set JAVA_HOME to its folder (not bin).')

def build():
    out = ROOT/'build/classes'; out.mkdir(parents=True, exist_ok=True)
    fx = ROOT/'vendor/javafx'
    if not list(fx.glob('*.jar')): raise SystemExit('JavaFX libraries missing; see START-HERE.md.')
    sources = list((ROOT/'Controller/src').rglob('*.java'))
    sources += list((ROOT/'TeamCode/src').rglob('*.java'))
    sources += list((ROOT/'ImportedTeamCode').rglob('*.java'))
    args = ['--release','21','--module-path',str(fx),'--add-modules','javafx.controls,javafx.fxml,javafx.swing',
            '-encoding','UTF-8','-cp',os.pathsep.join(str(p) for p in (ROOT/'lib').glob('*.jar')),'-d',str(out)] + [str(p) for p in sources]
    response = ROOT/'build/javac.args'
    response.write_text('\n'.join('"'+s.replace('\\','/')+'"' for s in args), encoding='utf-8')
    subprocess.run([str(jdk()/'bin/javac'), '@'+str(response)], cwd=ROOT, check=True)
    return out

def main():
    out=build()
    if '--build-only' in sys.argv: return
    if '--test' in sys.argv:
        subprocess.run([str(jdk()/'bin/java'),'-ea','-cp',str(out),'virtual_robot.biobuzz.PhysicsTests'],cwd=ROOT,check=True)
        return
    cp=os.pathsep.join([str(out),str(ROOT/'Controller/src'),str(ROOT/'TeamCode/src'),str(ROOT/'lib/*')])
    subprocess.run([str(jdk()/'bin/java'),'-Djavafx.cachedir='+str(ROOT/'build/fx-cache'),'--module-path',str(ROOT/'vendor/javafx'),
        '--add-modules','javafx.controls,javafx.fxml,javafx.swing','-cp',cp,
        'virtual_robot.controller.VirtualRobotApplication']+sys.argv[1:],cwd=ROOT,check=True)

if __name__=='__main__': main()
