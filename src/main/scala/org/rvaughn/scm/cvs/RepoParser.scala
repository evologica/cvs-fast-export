/*
 * Copyright (c) 2011 Roger Vaughn
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
import java.io.File
import java.io.PrintStream
import annotation.tailrec
import org.apache.tools.ant.DirectoryScanner

package org.rvaughn.scm.cvs {


class RepoParser(dir: String, log: PrintStream) {
    // the directory containing the CVS archives to import
    val base = dir
    // the complete list of CVS archive files to import, relative to "base"
    val paths = scanPaths(new File(base)).sorted
    // optional: the directory containing the actual CVS repository root
    val repo = StringCache(findRepo(dir))

    // find out where the repository starts (if at all)
    @tailrec private
    def findRepo(path: String): String = {
      if (path == null) {
        null
      } else if (new File(path, "CVSROOT").exists) {
        path
      } else {
        findRepo(new File(path).getParent)
      }
    }

    def scanPaths(dir: File): List[String] = {
      val directoryScanner = new DirectoryScanner()
      val includes = Array("**/*,v")
      val excludes = Array("**/.directory_history,v") ++ Config.excludePattern

      directoryScanner.setIncludes(includes)
      directoryScanner.setExcludes(excludes)

      directoryScanner.setBasedir(dir.getAbsolutePath)
      directoryScanner.setCaseSensitive(false)

      directoryScanner.scan

      directoryScanner.getIncludedFiles.toList
    }

    def parse(path: String, f: (CvsFile) => Unit) {
      f(new FileParser(new File(base, path), log).getFile(base, path.dropRight(2), repo))
    }

    def foreach(f: CvsFile => Unit) {
      paths.foreach(path => parse(path, f))
    }

    def fileCount: Int = {
      paths.size
    }
  }

/*
 * paths are interpretted like so:
 *
 * |------------ base ------------| |-------- output path ---------------|
 * /subdir1/subdir2/subdir3/subdir4/subdir5/subdir6/subdir7/sourcefile.txt,v
 * |---- repo ----| |-- module ---| |-- relative path ----| |-- rcs file --|
 *
 * If the repository cannot be identified, then repo is considered to be
 * equal to base.
 *
 * We'll normally take (base - repo) as the module, and assume
 * everything else is the relative path. If repo cannot be identified,
 * then we assume that module is the last component of base.
 */

}
