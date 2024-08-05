
  The Report task is the heart of the Apache Rat Ant Task Library, it
  runs Rat on a given set of resources and generates the report.

  The task can work on any Ant
  {{{https://ant.apache.org/manual/Types/resources.html}resource or
  resource collection}} and the usual Ant selectors can be applied to
  restrict things even further.

  Reports can use Rat's internal XML or plain text format or be styled
  by a custom XSLT stylesheet.

  It is possible to define custom matchers for licenses not directly supported
  by Rat via nested elements to the Report task.
