#!/usr/bin/perl -w
use strict;
use warnings;

my $rootDestination = '../app/src/main/res/drawable';
unless(-d $rootDestination) {
    die "Unknown source.  Expected to find $rootDestination.\n";
}

my %DirToResolution = (
    mdpi =>     32,
    hdpi =>     48,
    xhdpi =>    64,
    xxhdpi =>   96,
    xxxhdpi =>  192,
);

my %necessaryUtilities = (
    identify => '/usr/bin/identify',
    convert =>  '/usr/bin/convert',
);
my @missingUtilities;
for my $utility (keys %necessaryUtilities) {
    if (! -e $necessaryUtilities{$utility}) {
        push @missingUtilities, $utility;
    }
}
if (@missingUtilities) {
    die "unable to find the following utilities: @missingUtilities\n";
}

my @inFiles = grep -f, @ARGV;

# filter file names and eliminate any invalid file names
# and warn the user if he entered bad file names.
my %good = map{$_,1}
    grep /^[a-z_\d]+\.(m|h|xh|xxh|xxxh)dpi.png$/, @inFiles;
if (my @bad = grep !$good{$_}, @inFiles) {
    print "Can't resize the following files: bad file names\n    "
        , (map {"\"$_\", "} @bad), "\n";;
    @inFiles = grep $good{$_}, @inFiles;
}
unless(@inFiles) {
    die "Usage image_file_0 .. image_file_N\n",
        , "    File names should be of format name.resolution.png\n"
        , "    where resolution is one of mdpi, hdpi, xhdpi, xxhdpi,\n"
        , "    or xxxhdpi.\n";
}
foreach my $inFile (@inFiles) {
    my $isNinePatch = $inFile =~ /\.9\.png$/;
    my $res = (grep $DirToResolution{$_}, split /\./, $inFile)[0];
    my $nightFile = $inFile =~ m{-night-} || $inFile =~ m{-night$};
    my $isPng = $inFile =~ m/\.png$/i;
    my ($xPixels, $yPixels) = do {
        my @cmd = ('/usr/bin/identify', $inFile);
        my $result = `@cmd`;
        my ($x, $y) = ($result =~ m{\s+(\d+)x(\d+)\s+});
    };
    my $outFile = $inFile;
    $outFile =~ s/\.$res//;
    $outFile =~ s/\.night//;
    if ($res) {
        if ($isNinePatch) {
            my $outDir = "${rootDestination}";
            $outDir .= "-night" if $nightFile;
            $outDir .= "-$res" if $res;
            unless ( -d $outDir) {
                mkdir $outDir || die "Failed to create $outDir\n";
            }
            print "moving $inFile nine patch file to $outDir/$outFile\n";
            my @cmd = ('/bin/cp', $inFile
                , "$outDir/$outFile", '2>&1');
            my $result = `@cmd`;
            die "\"@cmd\" FAILED:$result\n" if $!;
        }
        else {
            my $xResize = (($xPixels - $DirToResolution{$res})
                / $DirToResolution{$res}) + 1;
            my $yResize = (($yPixels - $DirToResolution{$res})
                / $DirToResolution{$res}) + 1;
            print "\n$inFile ============\n";
            foreach my $resName (sort keys %DirToResolution) {
                my $outDir = "${rootDestination}";
                $outDir .= "-night" if $nightFile;
                $outDir .= "-$resName";
                unless ( -d $outDir) {
                    mkdir $outDir || die "Failed to create $outDir\n";
                }
                my $xCalculated
                    = int (($xResize * $DirToResolution{$resName}) + 0.5);
                my $yCalculated
                    = int (($yResize * $DirToResolution{$resName}) + 0.5);
                printf("%60s: x: %3d, y: %3d\n"
                    , "$outDir/$outFile", $xCalculated, $yCalculated);
                if ($outDir =~ m{-$res$}) {
                    my @cmd = ('/bin/cp', $inFile, "$outDir/$outFile");
                    system(@cmd) && die "\"@cmd\" FAILED: $!\n";
                }
                else {
                    my $geometry = "${xCalculated}x${yCalculated}!";
                    my @cmd = ($necessaryUtilities{convert}, $inFile,
                        '-resize', $geometry, "$outDir/$outFile");
                    system(@cmd) && die "\"@cmd\" FAILED\n";
                }
            }
        }
    }
}
