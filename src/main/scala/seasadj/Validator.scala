package org.cmhh.seasadj

import scala.util.{Try, Success, Failure}

/**
 * Singleton object used to validate specifications.
 * 
 * Based on the X13-ARIMA-SEATS documentation, an effort has been made to 
 * ensure that only valid combinations of specs and variables are permitted,
 * but some of the docs may have been misinterpreted during implementation.
 * Values are often checked, but usually only where a SpecType is defined.
 * Many spec values are simply permitted as Strings with no additional
 * validation, but this will likely improve over time.
 */
object Validator {
  /**
    * Validate [[Spec]].
    *
    * @param name Specification name, e.g. series, x11.
    * @param spec [[Spec]], e.g. ("sigmalim" -> SpecSigmaLim("[1.8, 2.8]"))
    * @return
    */
  def validateSpec(name: String, spec: Spec): Boolean = true

  /**
    * 
    *
    * @param specs
    * @return
    */
  def validateSpecs(specs: Specs): Boolean = true

  /**
   * Validate [[Specification]].
   *
   * @param spec specification
   */
  def validate(spec: Specification): Boolean = true
 
  /**
   * Validate [[Specifications]].
   *
   * @param specs specifications
   */
  def validate(specs: Specifications): Boolean = 
    !specs.map(spec => validate(spec)).contains(false)

  /**
   * Return a [[SpecValue]] given [[String]] input.
   *
   * @param spec name of spec, e.g. `x11`, `series`, `seats`, etc.
   * @param parameter name of parameter, e.g. `data`, `file`, `sigmalim`, etc.
   * @param value string containing the parameter value
   * @param inputType either `STRING` or `JSON`, e.g. `"(1,2,3)"` for string, or `[1,2,3]` for JSON
   */
  def specValue(spec: String, parameter: String, value: String, inputType: InputType = STRING): Try[SpecValue] = {
    Try({
      if (!RULES.contains(spec.toLowerCase)) {
        throw new IllegalArgumentException(s"Unknown spec, '$spec'.")
      } else if (!RULES(spec.toLowerCase).contains(parameter.toLowerCase)) {
        throw new IllegalArgumentException(s"'$parameter' is not an invalide parameterfor spec '$spec'.")
      } else {
        val specType = RULES(spec)(parameter)
        specType match {
          case "SpecString" => SpecString(value, inputType)
          case "SpecStringArray" => SpecStringArray(value, inputType)
          case "SpecCompType" => SpecCompType(value, inputType)
          case "SpecNum" => SpecNum(value, inputType)
          case "SpecNumArray" => SpecNumArray(value, inputType)
          case "SpecDate" => SpecDate(value, inputType)
          case "SpecInt" => SpecInt(value, inputType)
          case "SpecSpan" => SpecSpan(value, inputType)
          case "SpecARMA" => SpecARMA(value, inputType)
          case "SpecARIMA" => SpecARIMA(value, inputType)
          case "SpecBool" => SpecBool(value, inputType)
          case "SpecOrder" => SpecOrder(value, inputType)
          case _ => throw new IllegalArgumentException(s"Unknown value type, $specType.")
        }
      }
    })
  }

  /**
   * Map used to look up valid types for specification parameters. 
   */ 
  val RULES = Map(
    "arima" -> Map(
      "ar" -> "SpecARMA", //
      "ma" -> "SpecARMA", //
      "model" -> "SpecARIMA", //
      "title" -> "SpecString" //
    ),
    "automdl" -> Map(
      "acceptdefault" -> "SpecBool", //
      "checkmu" -> "SpecBool", //
      "diff" -> "SpecOrder", //
      "fsctlim" -> "SpecNum", // > 0.0
      "ljungboxlimit" -> "SpecString",
      "maxdiff" -> "SpecOrder", //
      "maxorder" -> "SpecOrder", //
      "mixed" -> "SpecBool", //
      "print" -> "SpecStringArray", // see docs
      "rejectfcst" -> "SpecBool", //
      "savelog" -> "SpecStringArray", // see docs
      "armalimit" -> "SpecNum", //
      "balanced" -> "SpecBool", //
      "exactdiff" -> "SpecString", // {yes, no, first}
      "hrinitial" -> "SpecBool", //
      "reducecv" -> "SpecNum", //
      "urfinal" -> "SpecNum" //
    ),
    "check" -> Map(
      "maxlag" -> "SpecString",
      "print" -> "SpecString",
      "qtype" -> "SpecString",
      "save" -> "SpecString",
      "savelog" -> "SpecString",
      "acflimit" -> "SpecString",
      "qlimit" -> "SpecString"
    ),
    "composite" -> Map(
      "appendbcst" -> "SpecString",
      "appendfcst" -> "SpecString",
      "decimals" -> "SpecString",
      "modelspan" -> "SpecString",
      "name" -> "SpecString",
      "print" -> "SpecString",
      "save" -> "SpecString",
      "savelog" -> "SpecString",
      "title" -> "SpecString",
      "type" -> "SpecString",
      "indoutlier" -> "SpecString",
      "saveprecision" -> "SpecString",
      "yr2000" -> "SpecString"
    ),
    "estimate" -> Map(
      "exact" -> "SpecString",
      "maxiter" -> "SpecString",
      "outofsample" -> "SpecString",
      "print" -> "SpecString",
      "save" -> "SpecString",
      "savelog" -> "SpecString",
      "tol" -> "SpecString",
      "file" -> "SpecString",
      "fix" -> "SpecString"
    ),
    "force" -> Map(
      "lambda" -> "SpecString",
      "mode" -> "SpecString",
      "print" -> "SpecString",
      "rho" -> "SpecString",
      "round" -> "SpecString",
      "save" -> "SpecString",
      "start" -> "SpecString",
      "target" -> "SpecString",
      "type" -> "SpecString",
      "usefcst" -> "SpecString",
      "indforce" -> "SpecString"
    ),
    "forecast" -> Map(
      "exclude" -> "SpecString",
      "lognormal" -> "SpecString",
      "maxback" -> "SpecString",
      "maxlead" -> "SpecString",
      "print" -> "SpecString",
      "probability" -> "SpecString",
      "save" -> "SpecString"
    ),
    "history" -> Map(
      "endtable" -> "SpecString",
      "estimates" -> "SpecString",
      "fixmdl" -> "SpecString",
      "fixreg" -> "SpecString",
      "fstep" -> "SpecString",
      "print" -> "SpecString",
      "sadjlags" -> "SpecString",
      "save" -> "SpecString",
      "savelog" -> "SpecString",
      "start" -> "SpecString",
      "target" -> "SpecString",
      "trendlags" -> "SpecString",
      "additivesa" -> "SpecString",
      "fixx11reg" -> "SpecString",
      "refresh" -> "SpecString",
      "outlier" -> "SpecString",
      "outlierwin" -> "SpecString",
      "transformfcst" -> "SpecString",
      "x11outlier" -> "SpecString"
    ),
    "identify" -> Map(
      "diff" -> "SpecString",
      "maxlag" -> "SpecString",
      "print" -> "SpecString",
      "save" -> "SpecString",
      "sdiff" -> "SpecString"
    ),
    "metadata" -> Map(
      "keys" -> "SpecString",
      "values" -> "SpecString"
    ),
    "outlier" -> Map(
      "critical" -> "SpecNumArray", // either a single number (for additive), or (AO, LS, TC)
      "lsrun" -> "SpecInt", //
      "method" -> "SpecString", // {addone, addall}
      "print" -> "SpecStringArray", // see docs
      "save" -> "SpecStringArray", // see docs
      "span" -> "SpecSpan", //
      "types" -> "SpecStringArray", // {none, ao, ls, tc, all}
      "almost" -> "SpecNum", //
      "tcrate" -> "SpecNum" // (0, 1), i.e. 0.0 < tcrate < 1.0
    ),
    "pickmdl" -> Map(
      "bcstlim" -> "SpecString",
      "fcstlim" -> "SpecString",
      "file" -> "SpecString",
      "identify" -> "SpecString",
      "method" -> "SpecString",
      "mode" -> "SpecString",
      "outofsample" -> "SpecString",
      "overdiff" -> "SpecString",
      "print" -> "SpecString",
      "qlim" -> "SpecString",
      "savelog" -> "SpecString"
    ),
    "regression" -> Map(
      "aicdiff" -> "SpecNumArray", // 
      "aictest" -> "SpecStringArray", // see docs
      "chi2test" -> "SpecBool", //
      "chi2testcv" -> "SpecNum", // (0.0, 1.0), i.e 0.0 < t < 1.0
      "file" -> "SpecString", // convert to data and remove later
      "format" -> "SpecString", // convert to data and remove later
      "data" -> "SpecNumArray", // might need to add in manually if data comes from 'file'
      "print" -> "SpecStringArray", // see docs
      "pvaictest" -> "SpecNum", // (0.0, 1.0), i.e 0.0 < t < 1.0
      "save" -> "SpecStringArray", // see docs
      "savelog" -> "SpecStringArray", // see docs
      "start" -> "SpecDate", // might need to add in manually if data comes from 'file'
      "testalleaster" -> "SpecBool", //
      "tlimit" -> "SpecNum", // > 0.0
      "user" -> "SpecStringArray", // see docs
      "usertype" -> "SpecStringArray", // {constant, seasonal, td, lpyear, lom, loq, ao, ls, user, holiday, holiday2, holiday3, holiday4, holiday5}
      "variables" -> "SpecStringArray", // see docs
      "b" -> "SpecARMA", //
      "centeruser" -> "SpecString", // {mean, seasonal}
      "eastermeans" -> "SpecBool",
      "noapply" -> "SpecStringArray", // {td, ao, ls, tc, holiday, userseasonal, user}
      "tcrate" -> "SpecNum" // (0, 1), i.e. 0.0 < tcrate < 1.0
    ),
    "seats" -> Map(
      "appendfcst" -> "SpecString",
      "finite" -> "SpecString",
      "hpcycle" -> "SpecString",
      "out" -> "SpecString",
      "print" -> "SpecString",
      "printphtrf" -> "SpecString",
      "qmax" -> "SpecString",
      "save" -> "SpecString",
      "savelog" -> "SpecString",
      "statseas" -> "SpecString",
      "tabtables" -> "SpecString",
      "bias" -> "SpecString",
      "epsiv" -> "SpecString",
      "epsphi" -> "SpecString",
      "hplan" -> "SpecString",
      "imean" -> "SpecString",
      "maxbias" -> "SpecString",
      "maxit" -> "SpecString",
      "noadmiss" -> "SpecString",
      "rmod" -> "SpecString",
      "xl" -> "SpecString"
    ),
    "series" -> Map(
      "appendbcst" -> "SpecBool", //
      "appendfcst" -> "SpecBool", //
      "comptype" -> "SpecCompType", //
      "compwt" -> "SpecInt", // > 0
      "file" -> "SpecString", // replace with data / start later
      "format" -> "SpecString", // replace with data / start later
      "data" -> "SpecNumArray", // might need to add in manually if data comes from 'file'
      "decimals" -> "SpecInt", // [0,5]
      "modelspan" -> "SpecSpan", //
      "name" -> "SpecString", //
      "period" -> "SpecInt", // {12, 4}
      "precision" -> "SpecInt", // [0,5]
      "print" -> "SpecStringArray", // see table 1.  not implemented
      "save" -> "SpecStringArray", // see table 1. not implemented
      "span" -> "SpecSpan", //
      "start" -> "SpecDate", // might need to add in manually if data comes from 'file'
      "title" -> "SpecString", //
      "type" -> "SpecString", // {flow, stock}
      "divpower" -> "SpecInt", // [-9, 9]
      "missingcode" -> "SpecInt", //
      "missingval" -> "SpecInt", //
      "saveprecision" -> "SpecInt", // [1,15]
      "trimzero" -> "SpecString", // {yes, no, span}
      "yr2000" -> "SpecBool" //
    ),
    "slidingspans" -> Map(
      "cutchng" -> "SpecString",
      "cutseas" -> "SpecString",
      "cuttd" -> "SpecString",
      "fixmdl" -> "SpecString",
      "fixreg" -> "SpecString",
      "length" -> "SpecString",
      "numspans" -> "SpecString",
      "outlier" -> "SpecString",
      "print" -> "SpecString",
      "save" -> "SpecString",
      "savelog" -> "SpecString",
      "start" -> "SpecString",
      "additivesa" -> "SpecString",
      "fixx11reg" -> "SpecString",
      "x11outlier" -> "SpecString"
    ),
    "spectrum" -> Map(
      "logqs" -> "SpecBool", //
      "print" -> "SpecStringArray", // See tables 1 and 8.  Not implemented.
      "qcheck" -> "SpecBool", //
      "save" -> "SpecStringArray", // See table 1.  Not implemented.
      "savelog" -> "SpecStringArray", // See tables 1 and 8.  Not implemented.
      "start" -> "SpecDate", //
      "decibel" -> "SpecBool", //
      "difference" -> "SpecString", // {yes, no, first}
      "maxar" -> "SpecInt", //
      "peakwidth" -> "SpecInt", //
      "series" -> "SpecString", // {original, a1, outlieradjoriginal, a19, adjoriginal, b1, modoriginal, e1}
      "siglevel" -> "SpecInt",
      "tukey120" -> "SpecBool",
      "type" -> "SpecString" // {arspec, periodogram}
    ),
    "transform" -> Map(
      "adjust" -> "SpecString", // {lom, loq, leapyear}
      "aicdiff" -> "SpecInt", //
      "file" -> "SpecString", // replace with data / start later
      "format" -> "SpecString", // replace with data / start later
      "data" -> "SpecNumArray", // might need to create from file and format
      "function" -> "SpecString", // {none, log, sqrt, inverse, logistic, auto}
      "mode" -> "SpecString", // {percent, ratio, diff}
      "name" -> "SpecString", //
      "power" -> "SpecInt", //
      "precision" -> "SpecInt", // [0, 5]
      "print" -> "SpecStringArray", // See table 1.  Not implemented.
      "save" -> "SpecStringArray", // See table 1.  Not implemented.
      "savelog" -> "SpecStringArray", // See table 2.  Not implemented.
      "start" -> "SpecDate", // might need to create from file and format
      "title" -> "SpecString", //
      "type" -> "SpecString", // {temporary, permanent}
      "constant" -> "SpecInt", //
      "trimzero" -> "SpecBool" //
    ),
    "x11" -> Map(
      "appendbcst" -> "SpecBool", //
      "appendfcst" -> "SpecBool", //
      "final" -> "SpecStringArray", // {all, ao, ls, tc, user}
      "mode" -> "SpecString", // {mult, add, logadd, pseudoadd}
      "print" -> "SpecStringArray", // see docs
      "save" -> "SpecStringArray", // see docs
      "savelog" -> "SpecStringArray", // see docs
      "seasonalma" -> "SpecStringArray", // {x11default, s3x1, s3x3, s3x5, s3x9, s3x15, stable, msr}
      "sigmalim" -> "SpecNumArray", // exactly two values e.g. (1.8 2.8)
      "title" -> "SpecString", //
      "trendma" -> "SpecInt", //  odd number <= 101
      "type" -> "SpecString", // {sa, summary, trend}
      "calendarsigma" -> "SpecString", // {all, signif, select, none}
      "keepholiday" -> "SpecBool", //
      "print1stpass" -> "SpecBool", //
      "sfshort" -> "SpecBool", //
      "sigmavec" -> "SpecStringArray", // list of months e.g. (jan feb dec)
      "trendic" -> "SpecNum", // > 0.0
      "true7term" -> "SpecBool" //
    ),
    "x11regression" -> Map(
      "aicdiff" -> "SpecString",
      "aictest" -> "SpecString",
      "critical" -> "SpecString",
      "file" -> "SpecString",
      "format" -> "SpecString",
      "outliermethod" -> "SpecString",
      "outlierspan" -> "SpecString",
      "print" -> "SpecString",
      "prior" -> "SpecString",
      "save" -> "SpecString",
      "savelog" -> "SpecString",
      "sigma" -> "SpecString",
      "span" -> "SpecString",
      "start" -> "SpecString",
      "tdprior" -> "SpecString",
      "user" -> "SpecString",
      "usertype" -> "SpecString",
      "variables" -> "SpecString",
      "almost" -> "SpecString",
      "b" -> "SpecString",
      "centeruser" -> "SpecString",
      "eastermeans" -> "SpecString",
      "forcecal" -> "SpecString",
      "noapply" -> "SpecString",
      "reweight" -> "SpecString",
      "umfile" -> "SpecString",
      "umformat" -> "SpecString",
      "umname" -> "SpecString",
      "umprecision" -> "SpecString",
      "umstart" -> "SpecString",
      "umtrimzero" -> "SpecString"
    )
  )
}
