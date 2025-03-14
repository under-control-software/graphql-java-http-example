/**
 * The purpose of this code is to show an example of serving a graphql query over HTTP
 * <p>
 * More info can be found here : http://graphql.org/learn/serving-over-http/
 * <p>
 * There are more concerns in a fully fledged application such as your approach to permissions
 * and authentication and so on that are not shown here.
 * <p>
 * The backing data is the "star wars" example schema.  A fairly complex example query is as follows :
 *
 * <pre>
 * {@code
 * {
 *      luke: human(id: "1000") {
 *          ...HumanFragment
 *      }
 *      leia: human(id: "1003") {
 *          ...HumanFragment
 *      }
 *  }
 *
 *  fragment HumanFragment on Human {
 *      name
 *      homePlanet
 *      friends {
 *      name
 *      __typename
 *  }
 * }
 *
 * }
 * </pre>{
 */
package com.graphql.example.http;
