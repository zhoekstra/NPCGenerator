open $FILES, "<./filenames.txt";
for $line(<$FILES>){
chomp $line;
print "mv $line $line.old";
system("mv $line $line.old");
}
