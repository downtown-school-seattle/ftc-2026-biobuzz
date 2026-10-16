{
  description = "FTC 2026 season code.";

  inputs = {
    nixpkgs.url = "github:nixos/nixpkgs/nixos-unstable";
  };

  outputs = {nixpkgs, ...}: let
    forAllSystems = function:
      nixpkgs.lib.genAttrs [
        "x86_64-linux"
      ] (system: function nixpkgs.legacyPackages.${system});
  in {
    devShell = forAllSystems (pkgs:
      pkgs.mkShell {
        buildInputs = with pkgs; [
          android-studio
        ];
      });
  };
}
