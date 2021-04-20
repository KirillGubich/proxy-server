## Proxy-server

A simple proxy server that logs proxied HTTP requests. The program works as a service and displays brief information
about proxied requests (URL and response code) in the form of a log. The proxy server supports blacklist filtering of
sites. The configuration file for the proxy server specifies a list of domains and / or URLs to block. When trying to
load a blacklisted page, the proxy server returns a predefined page with an error message.