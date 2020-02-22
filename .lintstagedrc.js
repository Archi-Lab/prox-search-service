const path = require("path");
module.exports = {
  "*.java": [
    absolutePaths => {
      let resolvedPaths = absolutePaths;

      if (process.platform === "win32") {
        resolvedPaths = absolutePaths
          .map(file => path.resolve(file))
          .map(file => file.split("\\").join("\\\\"));
        return `./mvnw.cmd spotless:apply -X -DspotlessFiles=${resolvedPaths.join(
          ","
        )}`;
      }

      return `./mvnw spotless:apply -X -DspotlessFiles=${resolvedPaths.join(
        ","
      )}`;
    },
    "git add"
  ],
  "*.{js,ts,css,scss,json,md,html,yml,yaml}": ["prettier --write", "git add"]
};
